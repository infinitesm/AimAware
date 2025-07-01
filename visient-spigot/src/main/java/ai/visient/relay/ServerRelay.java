package ai.visient.relay;

import ai.visient.Visient;
import ai.visient.profile.Profile;
import ai.visient.profile.tracker.impl.EntityTracker;
import ai.visient.profile.tracker.impl.PositionTracker;
import ai.visient.packet.wrapper.WrappedPacket;
import ai.visient.packet.wrapper.inbound.CPacketUseEntity;
import ai.visient.util.*;
import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ServerRelay {

    private static final int SAMPLES_THRESHOLD = 50;
    private static final int TARGET_TIMEOUT_TICKS = 60;
    private static final double RANGE_SQUARED = 10 * 10;

    private final Profile profile;
    private final EntityTracker entityTracker;
    private final PositionTracker positionTracker;

    private final Map<String, List<Double>> sampleBuffers = new LinkedHashMap<>();

    private TrackedEntity target;
    private int lastAction;
    private Double lastDeltaYaw;
    private Double lastDeltaPitch;

    private static final String[] FEATURE_NAMES = {
            "deltaYaw", "deltaPitch",
            "accelerationYaw", "accelerationPitch",
            "interceptX", "interceptY"
    };

    public ServerRelay(Profile profile) {
        this.profile = profile;
        this.entityTracker = profile.getTracker(EntityTracker.class);
        this.positionTracker = profile.getTracker(PositionTracker.class);

        for (String name : FEATURE_NAMES) {
            sampleBuffers.put(name, Lists.newArrayList());
        }
    }

    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;
            target = entityTracker.get(wrapper.getEntityId());

            if (target != null) {
                lastAction = profile.getTick();
            }
        }
    }

    public void handle(MouseSnapshot snapshot) {
        double deltaYaw = snapshot.getDeltaYaw();
        double deltaPitch = snapshot.getDeltaPitch();

        if (shouldCollect(deltaYaw, deltaPitch)) {
            double accelerationYaw = (lastDeltaYaw == null) ? 0 : deltaYaw - lastDeltaYaw;
            double accelerationPitch = (lastDeltaPitch == null) ? 0 : deltaPitch - lastDeltaPitch;
            double interceptX = snapshot.getInterceptX();
            double interceptY = snapshot.getInterceptY();

            sampleBuffers.get("deltaYaw").add(deltaYaw);
            sampleBuffers.get("deltaPitch").add(deltaPitch);
            sampleBuffers.get("accelerationYaw").add(accelerationYaw);
            sampleBuffers.get("accelerationPitch").add(accelerationPitch);
            sampleBuffers.get("interceptX").add(interceptX);
            sampleBuffers.get("interceptY").add(interceptY);

            if (sampleBuffers.get("deltaYaw").size() >= SAMPLES_THRESHOLD) {
                JSONObject jsonPayload = buildFeaturePayload();
                postJson(jsonPayload);
                clearSamples();
            }
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }

    private boolean shouldCollect(double deltaYaw, double deltaPitch) {
        if (profile.getTick() - lastAction > TARGET_TIMEOUT_TICKS) return false;
        if (deltaYaw * deltaPitch == 0) return false;
        if (!inRangeOfTarget()) return false;
        return true;
    }

    private boolean inRangeOfTarget() {
        if (target == null) return false;

        BoundingBox targetBox = target.revealBounds();

        double targetX = (targetBox.getMaxX() + targetBox.getMinX()) / 2.0;
        double targetY = targetBox.getMinY();
        double targetZ = (targetBox.getMaxZ() + targetBox.getMinZ()) / 2.0;

        double playerX = positionTracker.getFrom().getX();
        double playerY = positionTracker.getFrom().getY();
        double playerZ = positionTracker.getFrom().getZ();

        double dx = targetX - playerX;
        double dy = targetY - playerY;
        double dz = targetZ - playerZ;

        return dx * dx + dy * dy + dz * dz < RANGE_SQUARED;
    }

    private JSONObject buildFeaturePayload() {
        JSONArray dataArray = new JSONArray();

        for (String feature : FEATURE_NAMES) {
            List<Double> values = sampleBuffers.get(feature);

            double mean = MathUtil.mean(values);
            double stdDev = MathUtil.deviation(values);
            double median = MathUtil.median(values);
            double iqr = MathUtil.iqr(values);
            double mad = MathUtil.mad(values);
            double min = MathUtil.min(values);
            double max = MathUtil.max(values);
            double range = max - min;
            double absoluteMean = MathUtil.absoluteMean(values);

            dataArray.put(round(mean));
            dataArray.put(round(stdDev));
            dataArray.put(round(median));
            dataArray.put(round(iqr));
            dataArray.put(round(mad));
            dataArray.put(round(min));
            dataArray.put(round(max));
            dataArray.put(round(range));
            dataArray.put(round(absoluteMean));
        }

        JSONObject json = new JSONObject();
        json.put("data", dataArray);
        return json;
    }

    private void clearSamples() {
        for (List<Double> list : sampleBuffers.values()) {
            list.clear();
        }
    }

    private void postJson(JSONObject jsonObject) {
        String endpoint = "http://127.0.0.1:8000/predict";

        Visient.getAsyncJsonClient().postJson(endpoint, jsonObject.toString(), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Bukkit.broadcastMessage("Visient | Server responded:");
                    Bukkit.broadcastMessage("Status: " + response.getStatusLine());
                    Bukkit.broadcastMessage("Body: " + responseBody);

                    JSONObject jsonResponse = new JSONObject(responseBody);
                    int prediction = jsonResponse.getInt("prediction");

                    if (prediction == 0) { // Predicted aim modification.
                        ChatUtil.debug("&7%s > Suspected aim modification", profile.getPlayer().getName());
                    }
                } catch (Exception e) {
                    Bukkit.broadcastMessage("Visient | Failed to parse response.");
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {
                Bukkit.broadcastMessage("Visient | HTTP request failed:");
                ex.printStackTrace();
            }

            @Override
            public void cancelled() {
                Bukkit.broadcastMessage("Visient | HTTP request was cancelled.");
            }
        });
    }

    private static double round(double value) {
        return Math.round(value * 1e6) / 1e6;
    }
}

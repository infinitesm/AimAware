package ai.visient.data.relay.impl;

import ai.visient.debug.DebugUtil;
import ai.visient.network.client.AsyncJsonClient;
import ai.visient.profile.model.Profile;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class InferenceRelay {

    private final Profile profile;
    private final AsyncJsonClient asyncJsonClient;

    private double threshold;

    public InferenceRelay(Profile profile) {
        this.profile = profile;
        this.asyncJsonClient = profile.getPlugin().getAsyncJsonClient();
    }

    public void handleFeatures(Map<String, Double> features) {
        // Build JSON payload
        JSONObject json = new JSONObject();
        json.put("uuid", profile.getUuid());
        json.put("features", new JSONObject(features));

        String payload = json.toString();

        asyncJsonClient.postInference(payload, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONObject result = new JSONObject(responseBody);

                    // Parse prediction and confidence
                    int prediction = result.optInt("prediction", -1);

                    JSONArray probability = result.optJSONArray("probability");
                    double confidence = 0.0;
                    if (probability != null && prediction >= 0 && prediction < probability.length()) {
                        confidence = probability.optDouble(prediction, 0.0);
                    }

                    // Map prediction to label
                    // TODO: Handle through JSON
                    String label;
                    switch (prediction) {
                        case 0:
                            label = "SUSPICIOUS";
                            threshold += confidence;
                            break;
                        case 1:
                            label = "HUMAN";
                            threshold = Math.max(0, threshold - confidence);
                            break;
                        default:
                            label = "NONE, ERROR";
                            break;
                    }

                    DebugUtil.debugInference(profile.getPlayer().getName(), label, confidence, threshold);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {
                System.err.println("Inference request failed for player: " + profile.getUuid());
                ex.printStackTrace();
            }

            @Override
            public void cancelled() {
                System.out.println("Inference request cancelled for player: " + profile.getUuid());
            }
        });
    }
}

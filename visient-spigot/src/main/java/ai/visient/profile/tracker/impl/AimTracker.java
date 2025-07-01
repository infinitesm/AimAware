package ai.visient.profile.tracker.impl;

import ai.visient.profile.Profile;
import ai.visient.profile.tracker.Tracker;
import ai.visient.profile.tracker.handler.PacketProcessor;
import ai.visient.packet.wrapper.WrappedPacket;
import ai.visient.packet.wrapper.inbound.CPacketChat;
import ai.visient.packet.wrapper.inbound.CPacketUseEntity;
import ai.visient.util.BoundingBox;
import ai.visient.util.PlayerLocation;
import ai.visient.util.MathUtil;
import ai.visient.util.Vertex;
import ai.visient.util.MouseSnapshot;
import ai.visient.util.ReachUtil;
import ai.visient.util.TrackedEntity;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import net.minecraft.server.v1_8_R3.MathHelper;
import org.bukkit.Bukkit;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AimTracker extends Tracker implements PacketProcessor {
    private TrackedEntity target = null;
    private MouseSnapshot snapshot;

    private String identifier;
    private double lastDeltaYaw, lastDeltaPitch;
    private int lastAttack;

    private final List<MouseSnapshot> snapshots = new ArrayList<>();

    public AimTracker(Profile profile) {
        super(profile);
    }

    public void update(PlayerLocation to, PlayerLocation from) {
        snapshot = null;

        double deltaYaw = to.getYaw() - from.getYaw();
        double deltaPitch = to.getPitch() - from.getPitch();

        double accelerationYaw = deltaYaw - lastDeltaYaw;
        double accelerationPitch = deltaPitch - lastDeltaPitch;

        snapshot:
        {
            if (target == null) break snapshot;

            double offsetAngle = handleOffsetAngle(from, target.revealBounds(), to.getYaw());
            double[] intercepts = handleIntercepts(to, from);

            this.snapshot = new MouseSnapshot.Builder()
                    .setTargetBox(target.revealBounds().copy())
                    .setDeltaYaw(deltaYaw)
                    .setDeltaPitch(deltaPitch)
                    .setAccelerationYaw(accelerationYaw)
                    .setAccelerationPitch(accelerationPitch)
                    .setInterceptX(intercepts[0])
                    .setInterceptY(intercepts[1])
                    .setOffsetFromCenter(offsetAngle)
                    .build();
        }

        if (identifier != null && snapshot != null) {
            if (snapshots.size() % 50 == 0) {
                Bukkit.broadcastMessage("Collecting data: (" + snapshots.size() + " / 500)");
            }

            if (profile.getTick() - lastAttack < 35) {
                snapshots.add(snapshot);
            }

            if (snapshots.size() >= 500) {
                writeCsv(snapshots, identifier);
                snapshots.clear();
            }
        }

        if (snapshot != null) {
            profile.getServerRelay().handle(snapshot);
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }

    private double handleOffsetAngle(PlayerLocation from, BoundingBox boundingBox, double yaw) {
        double distanceX = boundingBox.posX() - from.getX();
        double distanceZ = boundingBox.posZ() - from.getZ();

        // Generate the yaw based on the distance using simple trig math. Wrap to 180F.
        float generatedYaw = MathHelper.g((float) (Math.toDegrees(Math.atan2(distanceZ, distanceX)) - 90F));

        // Add the offset from the actual yaw and generated yaw to the sample.
        return interiorAngle((float) yaw, generatedYaw);
    }

    public double[] handleIntercepts(PlayerLocation to, PlayerLocation from) {
        Vertex[] vertices = getVertices();

        float[] yaws = new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        float[] pitches = new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};

        for (Vertex vt : vertices) {
            //data.getPlayer().getWorld().playEffect(new Location(data.getPlayer().getWorld(), vt.getX(), vt.getY(), vt.getZ()), Effect.COLOURED_DUST, 5);
            double[][] camera = ReachUtil.obtainCameraPositions(profile);
            double[] vertex = vt.toArray();

            for (double[] campos : camera) {
                float[] rotations = ReachUtil.getRotations(campos, vertex);

                float yaw = rotations[0];
                float pitch = rotations[1];

                yaws[0] = Math.min(yaws[0], yaw);
                yaws[1] = Math.max(yaws[1], yaw);
                pitches[0] = Math.min(pitches[0], pitch);
                pitches[1] = Math.max(pitches[1], pitch);
            }
        }

        float playerYaw = to.getYaw();
        float playerPitch = to.getPitch();

        playerYaw = fixYaw(playerYaw);

        float rangeYaw = interiorAngle(yaws[1], yaws[0]);
        float rangePitch = interiorAngle(pitches[1], pitches[0]);

        // this shows where on the target box the player hit, on interceptX 0.0 is the left edge, 1.0 the right, same concept for pitch.
        double interceptX = (playerYaw - fixYaw(yaws[0])) / rangeYaw;
        double interceptY = (playerPitch - pitches[0]) / rangePitch;

        return new double[]{
                interceptX,
                interceptY
        };
    }

    private float interiorAngle(float a, float b) {
        return MathUtil.interiorAngle(a, b);
    }

    private float fixYaw(double v) {
        return (float) (v % 360f + 360f) % 360f;
    }

    private Vertex[] getVertices() {
        BoundingBox box = target.revealBounds().copy().expand(0.25F, 0.25F, 0.25F);

        return new Vertex[]{
                new Vertex(box.getMinX(), box.getMinY(), box.getMinZ()),
                new Vertex(box.getMinX(), box.getMinY(), box.getMaxZ()),
                new Vertex(box.getMaxX(), box.getMinY(), box.getMinZ()),
                new Vertex(box.getMaxX(), box.getMinY(), box.getMaxZ()),
                new Vertex(box.getMinX(), box.getMaxY(), box.getMinZ()),
                new Vertex(box.getMinX(), box.getMaxY(), box.getMaxZ()),
                new Vertex(box.getMaxX(), box.getMaxY(), box.getMinZ()),
                new Vertex(box.getMaxX(), box.getMaxY(), box.getMaxZ())
        };
    }

    private void writeCsv(List<MouseSnapshot> mouseSnapshots, String identifier) {
        String filepath = "C:\\Users\\rubik\\Downloads\\ACServer\\plugins\\Solora\\aim_data.csv";
        int size = 0;

        try (CSVReader reader = new CSVReader(new FileReader(filepath))) {
            while (reader.readNext() != null) {
                size++;
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filepath, true))) {
            // Writing header
            if (size == 0) {
                String[] header = {
                        "class",
                        "rotation_dyaw", "rotation_dpitch",
                        "rotation_ddyaw", "rotation_ddpitch",
                        "intercept_x", "intercept_y",
                        "center_angle_offset",
                        "t_min_x", "t_min_y", "t_min_z",
                        "t_max_x", "t_max_y", "t_max_z"
                };

                writer.writeNext(header);
            }

            // Writing data
            for (MouseSnapshot snapshot : mouseSnapshots) {
                String[] data = {
                        identifier,
                        String.valueOf(snapshot.getDeltaYaw()),
                        String.valueOf(snapshot.getDeltaPitch()),
                        String.valueOf(snapshot.getAccelerationYaw()),
                        String.valueOf(snapshot.getAccelerationPitch()),
                        String.valueOf(snapshot.getInterceptX()),
                        String.valueOf(snapshot.getInterceptY()),
                        String.valueOf(snapshot.getOffsetFromCenter()),
                        String.valueOf(snapshot.getTargetBox().minX),
                        String.valueOf(snapshot.getTargetBox().minY),
                        String.valueOf(snapshot.getTargetBox().minZ),
                        String.valueOf(snapshot.getTargetBox().maxX),
                        String.valueOf(snapshot.getTargetBox().maxY),
                        String.valueOf(snapshot.getTargetBox().maxZ)
                };
                writer.writeNext(data, false);
            }

            Bukkit.broadcastMessage("CSV file updated successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WrappedPacket packet) {
        EntityTracker entityTracker = profile.getTracker(EntityTracker.class);

        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;
            target = entityTracker.get(wrapper.getEntityId());
            lastAttack = profile.getTick();
        } else if (packet instanceof CPacketChat) {
            String message = ((CPacketChat) packet).getMessage();

            String[] args = message.split(" ");

            if (args[0].equals("/solora")) {
                if (args.length == 1) {
                    profile.getPlayer().sendMessage("Usage: /solora datacollect <identifier|stop>");
                    return;
                }

                if (args[1].equalsIgnoreCase("datacollect")) {
                    if (args.length == 2) {
                        profile.getPlayer().sendMessage("/solora datacollect <identifier|stop>");
                        return;
                    }

                    if (!args[2].equalsIgnoreCase("stop")) {
                        this.identifier = args[2];
                        profile.getPlayer().sendMessage("Collecting aim data identifier \"" + identifier + "\"");
                    } else {
                        profile.getPlayer().sendMessage("Stopped collecting aim data for " + "\"" + identifier + "\"");
                        this.identifier = null;
                    }
                }

            }
        }
    }
}

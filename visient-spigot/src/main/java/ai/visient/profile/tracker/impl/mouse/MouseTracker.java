package ai.visient.profile.tracker.impl.mouse;

import ai.visient.network.packet.wrapper.base.WrappedPacket;
import ai.visient.network.packet.wrapper.inbound.CPacketUseEntity;
import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.base.Tracker;
import ai.visient.profile.tracker.handler.PacketHandler;
import ai.visient.profile.tracker.impl.entity.EntityTracker;
import ai.visient.profile.tracker.impl.entity.util.BoundingBox;
import ai.visient.profile.tracker.impl.entity.util.TrackedEntity;
import ai.visient.profile.tracker.impl.mouse.util.MouseSnapshot;
import ai.visient.profile.tracker.impl.mouse.util.Vertex;
import ai.visient.profile.tracker.impl.position.PositionTracker;
import ai.visient.profile.tracker.impl.position.util.PlayerLocation;
import net.minecraft.server.v1_8_R3.MathHelper;

/**
 * Tracks all mouse movements and aiming data onto a target.
 */
public class MouseTracker extends Tracker implements PacketHandler {

    private TrackedEntity target;
    private Float lastDeltaYaw;
    private Float lastDeltaPitch;

    public MouseTracker(Profile profile) {
        super(profile);
    }

    @Override
    public void process(WrappedPacket packet) {
        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;

            EntityTracker tracker = profile.getTracker(EntityTracker.class);
            target = tracker.get(wrapper.getEntityId());
        }
    }

    /**
     * Update the mouse tracker.
     * @param to current player location
     * @param from previous player location
     */
    public void update(PlayerLocation to, PlayerLocation from) {
        float deltaYaw = to.getYaw() - from.getYaw();
        float deltaPitch = to.getPitch() - from.getPitch();

        if (target == null || lastDeltaYaw == null || lastDeltaPitch == null) {
            lastDeltaYaw = deltaYaw;
            lastDeltaPitch = deltaPitch;
            return;
        }

        // 2nd kinematic derivative using finite differences
        float accelerationYaw = deltaYaw - lastDeltaYaw;
        float accelerationPitch = deltaPitch - lastDeltaPitch;

        // Calculate target relevant info
        double offsetAngle = computeOffsetAngle(from, target.revealBounds(), to.getYaw());
        double[] intercepts = computeIntercepts(to);

        // Build the mouse snapshot
        MouseSnapshot snapshot = MouseSnapshot.builder()
                .targetBox(target.revealBounds().copy())
                .deltaYaw(deltaYaw)
                .deltaPitch(deltaPitch)
                .accelerationYaw(accelerationYaw)
                .accelerationPitch(accelerationPitch)
                .interceptX(intercepts[0])
                .interceptY(intercepts[1])
                .offsetFromCenter(offsetAngle)
                .build();

        // Pass through relay manager for further handling
        profile.getRelayManager().handle(snapshot);

        // Update the previous deltas for next tick calculation
        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }

    /**
     * Computes the offset angle between the player's yaw and the direction to the entity's bounding box center.
     */
    private double computeOffsetAngle(PlayerLocation from, BoundingBox boundingBox, double yaw) {
        double dx = boundingBox.posX() - from.getX();
        double dz = boundingBox.posZ() - from.getZ();

        float calculatedYaw = MathHelper.g((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F));

        return computeInteriorAngle((float) yaw, calculatedYaw);
    }

    /**
     * Computes intercept percentages (X,Y) of the player’s aim on the target bounding box.
     * This is kind of a jank method, but actually works quite well for our application since it lets us calculate
     * out of box "intercepts"
     */
    private double[] computeIntercepts(PlayerLocation to) {
        Vertex[] vertices = generateBoundingBoxVertices();

        float[] yawBounds = {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        float[] pitchBounds = {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};

        double[][] cameraPositions = obtainCameraPositions();

        for (Vertex vertex : vertices) {
            double[] vertexPos = vertex.toArray();

            for (double[] cameraPosition : cameraPositions) {
                float[] rotations = computeRotations(cameraPosition, vertexPos);

                yawBounds[0] = Math.min(yawBounds[0], rotations[0]);
                yawBounds[1] = Math.max(yawBounds[1], rotations[0]);

                pitchBounds[0] = Math.min(pitchBounds[0], rotations[1]);
                pitchBounds[1] = Math.max(pitchBounds[1], rotations[1]);
            }
        }

        float playerYaw = normalizeYaw(to.getYaw());
        float playerPitch = to.getPitch();

        float yawRange = computeInteriorAngle(yawBounds[1], yawBounds[0]);
        float pitchRange = computeInteriorAngle(pitchBounds[1], pitchBounds[0]);

        double interceptX = (playerYaw - normalizeYaw(yawBounds[0])) / yawRange;
        double interceptY = (playerPitch - pitchBounds[0]) / pitchRange;

        return new double[]{interceptX, interceptY};
    }

    /**
     * Converts a pair of points into yaw/pitch rotations.
     */
    private float[] computeRotations(double[] from, double[] to) {
        double dx = to[0] - from[0];
        double dy = to[1] - from[1];
        double dz = to[2] - from[2];

        double distanceXZ = Math.hypot(dx, dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        return new float[]{normalizeYaw(yaw), pitch};
    }

    /**
     * Expands the target bounding box and returns all 8 vertices.
     */
    private Vertex[] generateBoundingBoxVertices() {
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

    /**
     * Returns the camera positions used for calculating angles to the bounding box.
     */
    private double[][] obtainCameraPositions() {
        PositionTracker positionTracker = profile.getTracker(PositionTracker.class);

        double x = positionTracker.getTo().getX();
        double y = positionTracker.getTo().getY();
        double z = positionTracker.getTo().getZ();

        return new double[][]{
                {x, y + 1.62, z},
                {x, y + 1.54, z}
        };
    }

    /**
     * Normalizes a yaw angle into [0, 360).
     */
    private float normalizeYaw(double yaw) {
        return (float) ((yaw % 360.0 + 360.0) % 360.0);
    }

    /**
     * Computes the smallest interior angle between two yaws.
     */
    private float computeInteriorAngle(float a, float b) {
        float diff = Math.abs(a - b);
        return diff > 180 ? 360 - diff : diff;
    }
}

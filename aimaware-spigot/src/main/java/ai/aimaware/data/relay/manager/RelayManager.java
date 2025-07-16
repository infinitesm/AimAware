package ai.aimaware.data.relay.manager;

import ai.aimaware.data.feature.FeatureExtractor;
import ai.aimaware.data.relay.impl.CollectionRelay;
import ai.aimaware.data.relay.impl.InferenceRelay;
import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import ai.aimaware.network.packet.wrapper.inbound.CPacketUseEntity;
import ai.aimaware.profile.model.Profile;
import ai.aimaware.profile.tracker.impl.entity.EntityTracker;
import ai.aimaware.profile.tracker.impl.entity.util.BoundingBox;
import ai.aimaware.profile.tracker.impl.entity.util.TrackedEntity;
import ai.aimaware.profile.tracker.impl.info.InfoTracker;
import ai.aimaware.profile.tracker.impl.info.enums.RelayMode;
import ai.aimaware.profile.tracker.impl.mouse.util.MouseSnapshot;
import ai.aimaware.profile.tracker.impl.position.PositionTracker;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * RelayManager performs:
 * - high-level signal filtering
 * - windowing of time-series data
 * - feature extraction
 * - dispatching data to relays for collection and inference
 */
public class RelayManager {

    // Number of samples to collect per time window
    private static final int SAMPLES_THRESHOLD = 50;

    // Maximum ticks to keep considering the same target relevant
    private static final int INTERACTION_TIMEOUT = 60;

    // Squared distance threshold for target range
    private static final double RANGE_LIM_SQR = 10 * 10;

    // Profile and trackers
    private final Profile profile;
    private final EntityTracker entityTracker;
    private final PositionTracker positionTracker;
    private final InfoTracker infoTracker;

    // Relays for live inference and data collection
    private final InferenceRelay inferenceRelay;
    private final CollectionRelay collectionRelay;

    // Multivariate time series data, raw signal holder.
    private final Map<String, List<Double>> signals = new LinkedHashMap<>();

    // Interaction tracking
    private TrackedEntity target;
    private Integer lastInteractionTick;

    // Rotation tracking
    private Double lastDeltaYaw;
    private Double lastDeltaPitch;

    /**
     * Constructs a new RelayManager for the given profile.
     *
     * @param profile the profile for which this relay manager operates
     */
    public RelayManager(Profile profile) {
        this.profile = profile;
        this.entityTracker = profile.getTracker(EntityTracker.class);
        this.positionTracker = profile.getTracker(PositionTracker.class);
        this.infoTracker = profile.getTracker(InfoTracker.class);

        this.inferenceRelay = new InferenceRelay(profile);
        this.collectionRelay = new CollectionRelay(profile);

        initializeSignals();
    }

    /**
     * Initializes signal lists for windowing.
     */
    private void initializeSignals() {
        signals.put("deltaYaw", new LinkedList<>());
        signals.put("deltaPitch", new LinkedList<>());
        signals.put("accelerationYaw", new LinkedList<>());
        signals.put("accelerationPitch", new LinkedList<>());
        signals.put("interceptX", new LinkedList<>());
        signals.put("interceptY", new LinkedList<>());
    }

    /**
     * Handles incoming packets that may indicate interactions.
     *
     * @param packet the wrapped NMS packet
     */
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;

            target = entityTracker.get(wrapper.getEntityId());

            if (target != null) {
                lastInteractionTick = profile.getTick();
            }
        }
    }

    /**
     * Handles new mouse snapshots and performs:
     * - signal collection
     * - windowing
     * - relay dispatch
     *
     * @param snapshot the captured mouse snapshot
     */
    public void handle(MouseSnapshot snapshot) {
        double deltaYaw = snapshot.getDeltaYaw();
        double deltaPitch = snapshot.getDeltaPitch();

        if (shouldCollect(deltaYaw, deltaPitch)) {
            double accelerationYaw = (lastDeltaYaw == null) ? 0 : deltaYaw - lastDeltaYaw;
            double accelerationPitch = (lastDeltaPitch == null) ? 0 : deltaPitch - lastDeltaPitch;
            double interceptX = snapshot.getInterceptX();
            double interceptY = snapshot.getInterceptY();

            signals.get("deltaYaw").add(deltaYaw);
            signals.get("deltaPitch").add(deltaPitch);
            signals.get("accelerationYaw").add(accelerationYaw);
            signals.get("accelerationPitch").add(accelerationPitch);
            signals.get("interceptX").add(interceptX);
            signals.get("interceptY").add(interceptY);

            if (signals.get("deltaYaw").size() >= SAMPLES_THRESHOLD) {
                Map<String, Double> extractedFeatures = FeatureExtractor.extractFeatures(signals);

                // Only can do inference or collection at one time.
                RelayMode relayMode = infoTracker.getRelayMode();

                // Send features to relays
                switch (relayMode) {
                    case INFERENCE:
                        inferenceRelay.handleFeatures(extractedFeatures);
                        break;
                    case COLLECTION:
                        collectionRelay.handleFeatures(extractedFeatures);
                        collectionRelay.handleSignals(signals);
                        break;
                    case NONE:
                        // Do nothing.
                        break;
                }

                clearSamples();
            }
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }

    /**
     * Determines whether signals should be collected
     * for the current tick.
     *
     * @param deltaYaw   delta yaw for this tick
     * @param deltaPitch delta pitch for this tick
     * @return true if conditions indicate relevant signals
     */
    private boolean shouldCollect(double deltaYaw, double deltaPitch) {
        // Player has not yet interacted with a target
        if (lastInteractionTick == null) {
            return false;
        }

        // Too long since last target interaction
        if (profile.getTick() - lastInteractionTick > INTERACTION_TIMEOUT) {
            return false;
        }

        // Player is not moving mouse, no relevant data
        if (deltaYaw == 0 && deltaPitch == 0) {
            return false;
        }

        // Returns true if player is within 10 blocks of the target, acceptable range for collection.
        return inRangeOfTarget();
    }


    /**
     * Determines if the tracked target is in range.
     * Assume that if the target is outside of this range, the aiming pattern is less relevant.
     *
     * @return true if target is in range
     */
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

        return dx * dx + dy * dy + dz * dz < RANGE_LIM_SQR;
    }

    /**
     * Clears collected signals after sending a window to relays.
     */
    private void clearSamples() {
        for (List<Double> list : signals.values()) {
            list.clear();
        }
    }
}

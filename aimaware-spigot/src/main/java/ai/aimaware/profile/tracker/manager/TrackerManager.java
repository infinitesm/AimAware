package ai.aimaware.profile.tracker.manager;

import ai.aimaware.profile.model.Profile;
import ai.aimaware.profile.tracker.base.Tracker;
import ai.aimaware.profile.tracker.handler.PacketHandler;
import ai.aimaware.profile.tracker.handler.PostPacketHandler;
import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import ai.aimaware.profile.tracker.impl.entity.EntityTracker;
import ai.aimaware.profile.tracker.impl.info.InfoTracker;
import ai.aimaware.profile.tracker.impl.mouse.MouseTracker;
import ai.aimaware.profile.tracker.impl.position.PositionTracker;
import ai.aimaware.profile.tracker.impl.transaction.TransactionTracker;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import lombok.Getter;

import java.util.Collection;

public class TrackerManager {

    @Getter private final ClassToInstanceMap<Tracker> trackerMap;

    public TrackerManager(Profile profile) {
        this.trackerMap = new ImmutableClassToInstanceMap.Builder<Tracker>()
                .put(MouseTracker.class, new MouseTracker(profile))
                .put(EntityTracker.class, new EntityTracker(profile))
                .put(TransactionTracker.class, new TransactionTracker(profile))
                .put(PositionTracker.class, new PositionTracker(profile))
                .put(InfoTracker.class, new InfoTracker(profile))
                .build();
    }

    /**
     * Handle all packets before relay manager execution
     * @param packet NMS packet wrapper
     */
    public void handlePacket(WrappedPacket packet) {
        Collection<Tracker> trackers = trackerMap.values();

        for (Tracker tracker : trackers) {
            if (tracker instanceof PacketHandler) {
                ((PacketHandler) tracker).process(packet);
            }
        }
    }

    /**
     * Handle all packets after relay manager execution
     * TODO: Only really useful for entity tracker, can possibly streamline this.
     * @param packet NMS packet wrapper
     */
    public void handlePostPacket(WrappedPacket packet) {
        Collection<Tracker> trackers = trackerMap.values();

        for (Tracker tracker : trackers) {
            if (tracker instanceof PostPacketHandler) {
                ((PostPacketHandler) tracker).postProcess(packet);
            }
        }
    }
}

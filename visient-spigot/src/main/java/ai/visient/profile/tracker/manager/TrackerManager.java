package ai.visient.profile.tracker.manager;

import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.base.Tracker;
import ai.visient.profile.tracker.handler.PacketHandler;
import ai.visient.profile.tracker.handler.PostPacketHandler;
import ai.visient.network.packet.wrapper.base.WrappedPacket;
import ai.visient.profile.tracker.impl.entity.EntityTracker;
import ai.visient.profile.tracker.impl.info.InfoTracker;
import ai.visient.profile.tracker.impl.mouse.MouseTracker;
import ai.visient.profile.tracker.impl.position.PositionTracker;
import ai.visient.profile.tracker.impl.transaction.TransactionTracker;
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

    public void handlePacket(WrappedPacket packet) {
        Collection<Tracker> trackers = trackerMap.values();

        for (Tracker tracker : trackers) {
            if (tracker instanceof PacketHandler) {
                ((PacketHandler) tracker).process(packet);
            }
        }
    }

    public void handlePostPacket(WrappedPacket packet) {
        Collection<Tracker> trackers = trackerMap.values();

        for (Tracker tracker : trackers) {
            if (tracker instanceof PostPacketHandler) {
                ((PostPacketHandler) tracker).postProcess(packet);
            }
        }
    }
}

package ai.visient.profile.tracker;

import ai.visient.profile.Profile;
import ai.visient.profile.tracker.handler.PacketProcessor;
import ai.visient.profile.tracker.handler.PostPacketProcessor;
import ai.visient.profile.tracker.impl.*;
import ai.visient.packet.wrapper.WrappedPacket;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import lombok.Getter;

import java.util.Collection;

public class TrackerManager {

    @Getter private final ClassToInstanceMap<Tracker> trackerMap;

    public TrackerManager(Profile profile) {
        this.trackerMap = new ImmutableClassToInstanceMap.Builder<Tracker>()
                .put(AimTracker.class, new AimTracker(profile))
                .put(EntityTracker.class, new EntityTracker(profile))
                .put(PingTracker.class, new PingTracker(profile))
                .put(PositionTracker.class, new PositionTracker(profile))
                .build();
    }

    public void handlePacket(WrappedPacket packet) {
        Collection<Tracker> trackers = trackerMap.values();

        for (Tracker tracker : trackers) {
            if (tracker instanceof PacketProcessor) {
                ((PacketProcessor) tracker).process(packet);
            }
        }
    }

    public void handlePostPacket(WrappedPacket packet) {
        Collection<Tracker> trackers = trackerMap.values();

        for (Tracker tracker : trackers) {
            if (tracker instanceof PostPacketProcessor) {
                ((PostPacketProcessor) tracker).postProcess(packet);
            }
        }
    }
}

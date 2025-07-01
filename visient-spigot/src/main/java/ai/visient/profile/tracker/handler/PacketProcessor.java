package ai.visient.profile.tracker.handler;

import ai.visient.packet.wrapper.WrappedPacket;

public interface PacketProcessor {
    // Processes a packet before the check chain for trackers.
    void process(WrappedPacket packet);
}

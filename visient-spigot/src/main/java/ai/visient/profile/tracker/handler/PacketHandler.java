package ai.visient.profile.tracker.handler;

import ai.visient.network.packet.wrapper.base.WrappedPacket;

public interface PacketHandler {
    // Processes a packet before the check chain for trackers.
    void process(WrappedPacket packet);
}

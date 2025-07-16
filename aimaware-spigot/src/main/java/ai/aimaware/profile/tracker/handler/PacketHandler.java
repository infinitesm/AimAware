package ai.aimaware.profile.tracker.handler;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;

public interface PacketHandler {
    // Processes a packet before the check chain for trackers.
    void process(WrappedPacket packet);
}

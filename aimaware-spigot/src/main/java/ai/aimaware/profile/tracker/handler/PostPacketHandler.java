package ai.aimaware.profile.tracker.handler;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;

public interface PostPacketHandler {
    // Process a packet after the check chain for a tracker.
    void postProcess(WrappedPacket packet);
}

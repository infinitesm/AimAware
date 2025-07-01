package ai.visient.profile.tracker.handler;

import ai.visient.network.packet.wrapper.base.WrappedPacket;

public interface PostPacketHandler {
    // Process a packet after the check chain for a tracker.
    void postProcess(WrappedPacket packet);
}

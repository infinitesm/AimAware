package ai.visient.profile.tracker.handler;

import ai.visient.packet.wrapper.WrappedPacket;

public interface PostPacketProcessor {
    // Process a packet after the check chain for a tracker.
    void postProcess(WrappedPacket packet);
}

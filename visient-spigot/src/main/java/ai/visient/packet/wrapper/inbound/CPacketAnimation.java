package ai.visient.packet.wrapper.inbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;

public class CPacketAnimation extends WrappedPacket {

    public CPacketAnimation(PacketPlayInArmAnimation instance) {
        super(instance, PacketPlayInArmAnimation.class);
    }

    public long getTimestamp() {
        return getField("timestamp");
    }
}

package ai.visient.packet.wrapper.inbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;

public class CPacketKeepAlive extends WrappedPacket {

    public CPacketKeepAlive(PacketPlayInKeepAlive instance) {
        super(instance, PacketPlayInKeepAlive.class);
    }

    public int getId() {
        return getField("a");
    }
}

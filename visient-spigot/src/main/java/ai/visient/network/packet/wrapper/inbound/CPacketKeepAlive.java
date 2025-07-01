package ai.visient.network.packet.wrapper.inbound;

import ai.visient.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;

public class CPacketKeepAlive extends WrappedPacket {

    public CPacketKeepAlive(PacketPlayInKeepAlive instance) {
        super(instance, PacketPlayInKeepAlive.class);
    }

    public int getId() {
        return getField("a");
    }
}

package ai.aimaware.network.packet.wrapper.outbound;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutKeepAlive;

public class SPacketKeepAlive extends WrappedPacket {

    public SPacketKeepAlive(PacketPlayOutKeepAlive instance) {
        super(instance, PacketPlayOutKeepAlive.class);
    }

    public int getId() {
        return getField("a");
    }


}

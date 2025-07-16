package ai.aimaware.network.packet.wrapper.inbound;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInChat;

public class CPacketChat extends WrappedPacket {

    public CPacketChat(Packet<?> instance) {
        super(instance, PacketPlayInChat.class);
    }

    public String getMessage() {
        return getField("a");
    }
}

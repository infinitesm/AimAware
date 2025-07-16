package ai.aimaware.network.packet.wrapper.inbound;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInHeldItemSlot;

public class CPacketHeldItemSlot extends WrappedPacket {

    public CPacketHeldItemSlot(PacketPlayInHeldItemSlot instance) {
        super(instance, PacketPlayInHeldItemSlot.class);
    }

    public int getSlot() {
        return getField("itemInHandIndex");
    }
}

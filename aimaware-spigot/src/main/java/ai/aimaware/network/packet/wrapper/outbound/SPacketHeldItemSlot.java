package ai.aimaware.network.packet.wrapper.outbound;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutHeldItemSlot;

public class SPacketHeldItemSlot extends WrappedPacket {

    public SPacketHeldItemSlot(PacketPlayOutHeldItemSlot instance) {
        super(instance, PacketPlayOutHeldItemSlot.class);
    }

    public int getSlot() {
        return getField("a");
    }
}

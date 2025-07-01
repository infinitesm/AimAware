package ai.visient.packet.wrapper.outbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutHeldItemSlot;

public class SPacketHeldItemSlot extends WrappedPacket {

    public SPacketHeldItemSlot(PacketPlayOutHeldItemSlot instance) {
        super(instance, PacketPlayOutHeldItemSlot.class);
    }

    public int getSlot() {
        return getField("a");
    }
}

package ai.visient.network.packet.wrapper.outbound;

import ai.visient.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

public class SPacketEntityDestroy extends WrappedPacket {

    public SPacketEntityDestroy(PacketPlayOutEntityDestroy instance) {
        super(instance, PacketPlayOutEntityDestroy.class);
    }

    public int[] getEntities() {
        return getField("a");
    }
}

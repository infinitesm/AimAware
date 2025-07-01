package ai.visient.network.packet.wrapper.outbound;

import ai.visient.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutRemoveEntityEffect;

public class SPacketRemoveEntityEffect extends WrappedPacket {

    public SPacketRemoveEntityEffect(PacketPlayOutRemoveEntityEffect instance) {
        super(instance, PacketPlayOutRemoveEntityEffect.class);
    }

    public int getEntityId() {
        return getField("a");
    }

    public int getEffectId() {
        return getField("b");
    }
}

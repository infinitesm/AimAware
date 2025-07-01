package ai.visient.network.packet.wrapper.outbound;

import ai.visient.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEffect;

public class SPacketEntityEffect extends WrappedPacket {

    public SPacketEntityEffect(PacketPlayOutEntityEffect instance) {
        super(instance, PacketPlayOutEntityEffect.class);
    }

    public int getEntityId() {
        return getField("a");
    }

    public byte getEffectId() {
        return getField("b");
    }

    public byte getAmplifier() {
        return getField("c");
    }

    public int getDuration() {
        return getField("d");
    }

    public boolean hideParticles() {
        return ((byte) getField("e")) == 1;
    }
}

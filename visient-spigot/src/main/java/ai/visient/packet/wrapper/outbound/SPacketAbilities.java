package ai.visient.packet.wrapper.outbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutAbilities;

public class SPacketAbilities extends WrappedPacket {

    public SPacketAbilities(PacketPlayOutAbilities instance) {
        super(instance, PacketPlayOutAbilities.class);
    }

    public boolean isInvulnerable() {
        return getField("a");
    }

    public boolean isFlying() {
        return getField("b");
    }

    public boolean allowsFlying() {
        return getField("c");
    }

    public boolean creativeMode() {
        return getField("d");
    }

    public float getFlySpeed() {
        return getField("e");
    }

    public float getWalkSpeed() {
        return getField("f");
    }
}

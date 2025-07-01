package ai.visient.network.packet.wrapper.inbound;

import ai.visient.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInAbilities;

public class CPacketAbilities extends WrappedPacket {

    public CPacketAbilities(PacketPlayInAbilities instance) {
        super(instance, PacketPlayInAbilities.class);
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

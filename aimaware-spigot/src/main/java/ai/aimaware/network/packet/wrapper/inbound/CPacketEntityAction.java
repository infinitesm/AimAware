package ai.aimaware.network.packet.wrapper.inbound;

import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;

public class CPacketEntityAction extends WrappedPacket {

    public CPacketEntityAction(PacketPlayInEntityAction instance) {
        super(instance, PacketPlayInEntityAction.class);
    }

    public int getEntityId() {
        return getField("a");
    }

    public PacketPlayInEntityAction.EnumPlayerAction getAction() {
        return getField("animation");
    }

    public int getAuxData() {
        return getField("c");
    }
}

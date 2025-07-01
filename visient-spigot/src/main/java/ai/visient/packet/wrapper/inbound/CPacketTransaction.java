package ai.visient.packet.wrapper.inbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInTransaction;

public class CPacketTransaction extends WrappedPacket {

    public CPacketTransaction(PacketPlayInTransaction instance) {
        super(instance, PacketPlayInTransaction.class);
    }

    public int getWindowId() {
        return getField("a");
    }

    public short getActionNumber() {
        return getField("b");
    }

    public boolean isAccepted() {
        return getField("c");
    }
}

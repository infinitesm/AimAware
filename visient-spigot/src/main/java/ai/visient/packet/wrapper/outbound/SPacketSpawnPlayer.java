package ai.visient.packet.wrapper.outbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;

import java.util.UUID;

public class SPacketSpawnPlayer extends WrappedPacket {

    public SPacketSpawnPlayer(PacketPlayOutNamedEntitySpawn instance) {
        super(instance, PacketPlayOutNamedEntitySpawn.class);
    }

    public int getEntityId() {
        return getField("a");
    }

    public UUID getUUID() {
        return getField("b");
    }

    public int getX() {
        return getField("c");
    }

    public int getY() {
        return getField("d");
    }

    public int getZ() {
        return getField("e");
    }

    public byte getYaw() {
        return getField("f");
    }

    public byte getPitch() {
        return getField("g");
    }
}

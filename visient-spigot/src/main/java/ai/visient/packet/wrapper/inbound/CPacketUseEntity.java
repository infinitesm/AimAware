package ai.visient.packet.wrapper.inbound;

import ai.visient.packet.wrapper.WrappedPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.util.Vector;

import java.util.Optional;

public class CPacketUseEntity extends WrappedPacket {

    public CPacketUseEntity(PacketPlayInUseEntity instance) {
        super(instance, PacketPlayInUseEntity.class);
    }

    public int getEntityId() {
        return getField("a");
    }

    public PacketPlayInUseEntity.EnumEntityUseAction getAction() {
        return getField("b");
    }

    public Optional<Vector> getVector() {
        Vec3D vec3D = getField("c");

        if (vec3D == null) return Optional.empty();

        return Optional.of(new Vector(vec3D.a, vec3D.b, vec3D.c));
    }
}

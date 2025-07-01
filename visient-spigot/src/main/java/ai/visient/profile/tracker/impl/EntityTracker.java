package ai.visient.profile.tracker.impl;

import ai.visient.profile.Profile;
import ai.visient.profile.tracker.Tracker;
import ai.visient.profile.tracker.handler.PacketProcessor;
import ai.visient.profile.tracker.handler.PostPacketProcessor;
import ai.visient.packet.wrapper.WrappedPacket;
import ai.visient.packet.wrapper.inbound.CPacketFlying;
import ai.visient.packet.wrapper.outbound.SPacketEntity;
import ai.visient.packet.wrapper.outbound.SPacketEntityDestroy;
import ai.visient.packet.wrapper.outbound.SPacketEntityTeleport;
import ai.visient.packet.wrapper.outbound.SPacketSpawnPlayer;
import ai.visient.util.BoundingBox;
import ai.visient.util.TrackedEntity;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.HashMap;
import java.util.Map;

public class EntityTracker extends Tracker implements PacketProcessor, PostPacketProcessor {

    public EntityTracker(Profile profile) {
        super(profile);
    }

    private final Map<Integer, TrackedEntity> entityMap = new HashMap<>();

    @Override
    public synchronized void process(WrappedPacket packet) {
        if (packet instanceof SPacketEntity) {
            SPacketEntity wrapper = (SPacketEntity) packet;
            TrackedEntity entity = entityMap.get(wrapper.getEntityId());

            if (entity == null) return;

            // Confirm using transactions for lag-proofing.
            profile.getTracker(PingTracker.class).confirm(() -> {
                double x = (entity.serverPosX + wrapper.getX()) / 32.0D;
                double y = (entity.serverPosY + wrapper.getY()) / 32.0D;
                double z = (entity.serverPosZ + wrapper.getZ()) / 32.0D;

                double w = entity.width;
                double h = entity.height;

                entity.confirmingBounds = new BoundingBox(x - w, y, z - w, x + w, y + h, z + w);
            });

            profile.getTracker(PingTracker.class).confirm(() -> {
                entity.confirmingBounds = null;

                int x = entity.serverPosX + wrapper.getX();
                int y = entity.serverPosY + wrapper.getY();
                int z = entity.serverPosZ + wrapper.getZ();

                entity.updateBounds(x, y, z);
            });
        } else if (packet instanceof SPacketEntityTeleport) {
            SPacketEntityTeleport wrapper = (SPacketEntityTeleport) packet;
            TrackedEntity entity = entityMap.get(wrapper.getEntityId());

            if (entity == null) return;

            profile.getTracker(PingTracker.class).confirm(() -> {
                double x = wrapper.getX() / 32.0D;
                double y = wrapper.getY() / 32.0D;
                double z = wrapper.getZ() / 32.0D;

                double w = entity.width;
                double h = entity.height;

                entity.confirmingBounds = new BoundingBox(x - w, y, z - w, x + w, y + h, z + w);
            });

            profile.getTracker(PingTracker.class).confirm(() -> {
                entity.confirmingBounds = null;

                int x = wrapper.getX();
                int y = wrapper.getY();
                int z = wrapper.getZ();

                entity.updateBounds(x, y, z);
            });
        } else if (packet instanceof SPacketSpawnPlayer) {
            SPacketSpawnPlayer wrapper = (SPacketSpawnPlayer) packet;

            if (wrapper.getEntityId() == profile.getPlayer().getEntityId()) return;

            CraftWorld craftWorld = (CraftWorld) profile.getPlayer().getWorld();
            net.minecraft.server.v1_8_R3.World nmsWorld = craftWorld.getHandle();

            if (!entityMap.containsKey(wrapper.getEntityId())) {
                TrackedEntity reachEntity = new TrackedEntity(
                        wrapper.getEntityId(),
                        wrapper.getX(),
                        wrapper.getY(),
                        wrapper.getZ(),
                        0.6F,
                        1.8F);

                entityMap.put(wrapper.getEntityId(), reachEntity);
            }
        } else if (packet instanceof SPacketEntityDestroy) {
            SPacketEntityDestroy wrapper = (SPacketEntityDestroy) packet;

            for (int id : wrapper.getEntities()) entityMap.remove(id);
        } else if (packet instanceof CPacketFlying) {
            for (TrackedEntity entity : this.entityMap.values()) {
                if (entity.confirmingBounds != null) {
                    entity.bounds.contain(entity.confirmingBounds);
                }
            }
        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            // Interpolation, super important for the reach check.
            entityMap.values().forEach(TrackedEntity::interpolate);
        }
    }

    public TrackedEntity get(int id) {
        return entityMap.get(id);
    }
}

package ai.visient.profile.tracker.impl.entity;

import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.base.Tracker;
import ai.visient.profile.tracker.handler.PacketHandler;
import ai.visient.profile.tracker.handler.PostPacketHandler;
import ai.visient.network.packet.wrapper.base.WrappedPacket;
import ai.visient.network.packet.wrapper.inbound.CPacketFlying;
import ai.visient.network.packet.wrapper.outbound.*;
import ai.visient.profile.tracker.impl.position.PositionTracker;
import ai.visient.profile.tracker.impl.transaction.TransactionTracker;
import ai.visient.profile.tracker.impl.entity.util.BoundingBox;
import ai.visient.profile.tracker.impl.entity.util.TrackedEntity;

import java.util.HashMap;
import java.util.Map;

public class EntityTracker extends Tracker implements PacketHandler, PostPacketHandler {

    private final Map<Integer, TrackedEntity> entityMap = new HashMap<>();

    public EntityTracker(Profile profile) {
        super(profile);
    }

    @Override
    public synchronized void process(WrappedPacket packet) {
        if (packet instanceof SPacketEntity) {
            SPacketEntity wrapper = (SPacketEntity) packet;
            handleRelativeMove(wrapper.getEntityId(), wrapper.getX(), wrapper.getY(), wrapper.getZ());
        }

        else if (packet instanceof SPacketEntityTeleport) {
            SPacketEntityTeleport wrapper = (SPacketEntityTeleport) packet;
            handleAbsoluteTeleport(wrapper.getEntityId(), wrapper.getX(), wrapper.getY(), wrapper.getZ());
        }

        else if (packet instanceof SPacketSpawnPlayer) {
            SPacketSpawnPlayer wrapper = (SPacketSpawnPlayer) packet;
            handleSpawnPlayer(wrapper);
        }

        else if (packet instanceof SPacketEntityDestroy) {
            SPacketEntityDestroy wrapper = (SPacketEntityDestroy) packet;
            handleEntityDestroy(wrapper);
        }

        else if (packet instanceof CPacketFlying) {
            confirmBoundingBoxes();
        }
    }

    private void handleRelativeMove(int entityId, int dx, int dy, int dz) {
        TrackedEntity entity = entityMap.get(entityId);
        if (entity == null) return;

        int newX = entity.compressedX + dx;
        int newY = entity.compressedY + dy;
        int newZ = entity.compressedZ + dz;

        queueConfirmBounds(entity,
                (newX / 32.0), (newY / 32.0), (newZ / 32.0),
                entity.width, entity.height
        );

        profile.getTracker(TransactionTracker.class).confirm(() ->
                entity.updateBounds(newX, newY, newZ)
        );
    }

    private void handleAbsoluteTeleport(int entityId, int x, int y, int z) {
        TrackedEntity entity = entityMap.get(entityId);
        if (entity == null) return;

        queueConfirmBounds(entity,
                (x / 32.0), (y / 32.0), (z / 32.0),
                entity.width, entity.height
        );

        profile.getTracker(TransactionTracker.class).confirm(() ->
                entity.updateBounds(x, y, z)
        );
    }

    private void handleSpawnPlayer(SPacketSpawnPlayer wrapper) {
        if (wrapper.getEntityId() == profile.getPlayer().getEntityId()) {
            return;
        }

        entityMap.computeIfAbsent(wrapper.getEntityId(), id ->
                new TrackedEntity(
                        id,
                        wrapper.getX(),
                        wrapper.getY(),
                        wrapper.getZ(),
                        0.6F,
                        1.8F
                )
        );
    }

    private void handleEntityDestroy(SPacketEntityDestroy wrapper) {
        for (int id : wrapper.getEntities()) {
            entityMap.remove(id);
        }
    }

    private void confirmBoundingBoxes() {
        for (TrackedEntity entity : entityMap.values()) {
            if (entity.potential != null) {
                entity.area.contain(entity.potential);
            }
        }
    }

    private void queueConfirmBounds(TrackedEntity entity, double x, double y, double z, double width, double height) {
        BoundingBox confirming = new BoundingBox(
                x - width, y, z - width,
                x + width, y + height, z + width
        );

        profile.getTracker(TransactionTracker.class).confirm(() ->
                entity.potential = confirming
        );

        profile.getTracker(TransactionTracker.class).confirm(() ->
                entity.potential = null
        );
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (profile.getTracker(PositionTracker.class).isTeleporting()) return;

            entityMap.values().forEach(TrackedEntity::interpolate);
        }
    }

    public TrackedEntity get(int id) {
        return entityMap.get(id);
    }
}

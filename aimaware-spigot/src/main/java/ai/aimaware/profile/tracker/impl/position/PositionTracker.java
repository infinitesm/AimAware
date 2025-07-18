package ai.aimaware.profile.tracker.impl.position;

import ai.aimaware.profile.model.Profile;
import ai.aimaware.profile.tracker.base.Tracker;
import ai.aimaware.profile.tracker.handler.PacketHandler;
import ai.aimaware.network.packet.wrapper.base.WrappedPacket;
import ai.aimaware.network.packet.wrapper.inbound.CPacketFlying;
import ai.aimaware.network.packet.wrapper.outbound.SPacketPosition;
import ai.aimaware.profile.tracker.impl.mouse.MouseTracker;
import ai.aimaware.profile.tracker.impl.position.util.PlayerLocation;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Deque;

@Getter
public class PositionTracker extends Tracker implements PacketHandler {

    // Teleports double ended queue for teleport tracking
    private final Deque<Vector> teleports = Lists.newLinkedList();

    // Location data
    private PlayerLocation from;
    private PlayerLocation to;

    // Various other data
    private double lastDistance;
    private int lastTeleportTick;
    private boolean teleporting;

    public PositionTracker(Profile profile) {
        super(profile);
    }

    /**
     * Packet routing for the position tracker
     * @param packet NMS packet wrapper
     */
    @Override
    public void process(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            CPacketFlying wrapper = (CPacketFlying) packet;
            handleFlyingPacket(wrapper);
        }
        else if (packet instanceof SPacketPosition) {
            SPacketPosition wrapper = (SPacketPosition) packet;
            handleServerTeleport(wrapper);
        }
    }

    private void handleFlyingPacket(CPacketFlying wrapper) {
        // Teleporting status only lasts one tick to prevent extra leniency.
        teleporting = false;

        // Shift last known "to" location to "from"
        if (to != null) {
            from = to.copy();
        }

        if (to == null) {
            // First packet seen, initialize
            to = new PlayerLocation(
                    wrapper.getX(),
                    wrapper.getY(),
                    wrapper.getZ(),
                    wrapper.getYaw(),
                    wrapper.getPitch(),
                    wrapper.isOnGround()
            );
        } else {
            updatePlayerLocation(wrapper);
            to.setTimestamp(System.currentTimeMillis());
        }

        if (from != null) {
            double distance = to.distanceSquared(from);

            checkTeleportCompletion();

            // Update mouse tracking with new movement
            profile.getTracker(MouseTracker.class).update(to, from);

            lastDistance = distance;
        }
    }

    private void updatePlayerLocation(CPacketFlying wrapper) {
        if (wrapper.isPosition()) {
            to.setX(wrapper.getX());
            to.setY(wrapper.getY());
            to.setZ(wrapper.getZ());
        }

        if (wrapper.isRotation()) {
            to.setYaw(wrapper.getYaw());
            to.setPitch(wrapper.getPitch());
        }

        to.setOnGround(wrapper.isOnGround());
    }

    private void checkTeleportCompletion() {
        Vector queuedTeleport = teleports.peek();

        if (queuedTeleport != null) {
            Vector current = new Vector(to.getX(), to.getY(), to.getZ());

            // Confirm teleport only if player reached the precise coords
            // TODO: The extra leniency is arbitrary, you can probably check for an exact match.
            if (queuedTeleport.distanceSquared(current) < 1e-7) {
                teleports.poll();
                teleporting = true;
                lastTeleportTick = profile.getTick();
            }
        }
    }

    private void handleServerTeleport(SPacketPosition wrapper) {
        // Note: multiplayer ignores teleport flags and always sends full positions
        teleports.add(new Vector(wrapper.getX(), wrapper.getY(), wrapper.getZ()));
    }
}

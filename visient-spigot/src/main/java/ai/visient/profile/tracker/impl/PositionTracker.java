package ai.visient.profile.tracker.impl;

import ai.visient.profile.Profile;
import ai.visient.profile.tracker.Tracker;
import ai.visient.profile.tracker.handler.PacketProcessor;
import ai.visient.packet.wrapper.WrappedPacket;
import ai.visient.packet.wrapper.inbound.CPacketFlying;
import ai.visient.packet.wrapper.outbound.SPacketPosition;
import ai.visient.util.PlayerLocation;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Deque;

@Getter
public class PositionTracker extends Tracker implements PacketProcessor {

    public PositionTracker(Profile profile) {
        super(profile);
    }

    private final Deque<Vector> teleports = Lists.newLinkedList();
    private PlayerLocation from, to;
    private double lastDistance;
    private int lastOffset, lastTeleport;

    @Override
    public void process(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            profile.getPlayer().setHealth(20);
            profile.getPlayer().setFoodLevel(20);

            CPacketFlying wrapper = (CPacketFlying) packet;

            // I know what you're going to say, shut the fuck up.
            if (to != null) from = to.copy();
            if (to == null) to = new PlayerLocation(wrapper.getX(), wrapper.getY(), wrapper.getZ(),
                    wrapper.getYaw(), wrapper.getPitch(), wrapper.isOnGround());
            else {
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
                to.setTimestamp(System.currentTimeMillis());
            }

            if (from != null) {
                double distance = to.distanceSquared(from);

                if (lastDistance > 0 && distance == 0 && !wrapper.isPosition()) lastOffset = profile.getTick();

                Vector teleport = teleports.peek();

                // Used to confirm if players received teleports. Yes, flags exist, they aren't used in multiplayer.
                if (teleport != null && teleport.distanceSquared(new Vector(to.getX(), to.getY(), to.getZ())) == 0) {
                    teleports.poll();
                    lastTeleport = profile.getTick();
                }

                profile.getTracker(AimTracker.class).update(to, from);

                lastDistance = distance;
            }
        } else if (packet instanceof SPacketPosition) {
            SPacketPosition wrapper = (SPacketPosition) packet;
            teleports.add(new Vector(wrapper.getX(), wrapper.getY(), wrapper.getZ()));
        }
    }

    // Basically a shitty fix for the 0.03 condition in the client, just increase the threshold for some checks.
    public boolean isOffsetMotion() {
        return profile.getTick() - lastOffset < 4;
    }

    // Different from isOffsetMotion, this is mainly used for reach checks, the former is used for motion checks which use movement deltas which are affected for longer.
    public boolean isOffsetPosition() {
        return profile.getTick() == lastOffset;
    }

    public boolean isTeleporting() {
        return profile.getTick() - lastTeleport == 0;
    }
}

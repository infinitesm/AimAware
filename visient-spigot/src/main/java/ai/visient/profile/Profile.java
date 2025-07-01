package ai.visient.profile;

import ai.visient.relay.ServerRelay;
import ai.visient.profile.tracker.Tracker;
import ai.visient.profile.tracker.TrackerManager;
import ai.visient.packet.manager.PacketManager;
import ai.visient.packet.wrapper.inbound.CPacketFlying;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class Profile {

    private final Player player;
    private final ServerRelay serverRelay;
    private final PacketManager packetManager;
    private final TrackerManager trackerManager;

    private int tick;

    public Profile(Player player) {
        this.player = player;

        this.packetManager = new PacketManager(this);
        this.trackerManager = new TrackerManager(this);
        this.serverRelay = new ServerRelay(this);

        // Start the packet manager.
        packetManager.registerHandlers();
        packetManager.start();

        // Add a listener to the packet handler.
        packetManager.addListener(packet -> {
            // Increment the ticks existed when the client moves.
            if (packet instanceof CPacketFlying) ++tick;

            // Handle the tracker processing for this packet.
            trackerManager.handlePacket(packet);

            // Feed the incoming packet to the sample collector.
            serverRelay.handle(packet);

            // Handle the tracker post-processing.
            trackerManager.handlePostPacket(packet);
        });
    }

    public <T extends Tracker> T  getTracker(Class<T> klass) {
        return trackerManager.getTrackerMap().getInstance(klass);
    }
}

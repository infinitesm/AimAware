package ai.visient.profile.model;

import ai.visient.VisientPlugin;
import ai.visient.data.relay.impl.InferenceRelay;
import ai.visient.data.relay.manager.RelayManager;
import ai.visient.profile.tracker.base.Tracker;
import ai.visient.profile.tracker.manager.TrackerManager;
import ai.visient.network.packet.manager.PacketManager;
import ai.visient.network.packet.wrapper.inbound.CPacketFlying;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class Profile {

    private final Player player;
    private final UUID uuid;
    private final VisientPlugin plugin;
    private final RelayManager relayManager;
    private final PacketManager packetManager;
    private final TrackerManager trackerManager;

    private int tick;

    public Profile(Player player, VisientPlugin plugin) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.plugin = plugin;

        this.packetManager = new PacketManager(this);
        this.trackerManager = new TrackerManager(this);
        this.relayManager = new RelayManager(this);

        // Start the packet manager.
        packetManager.registerHandlers();
        packetManager.start();

        // Add a listener to the packet handler.
        packetManager.addListener(packet -> {
            // Increment the ticks existed when the client moves.
            if (packet instanceof CPacketFlying) ++tick;

            // Handle the tracker processing for this packet.
            trackerManager.handlePacket(packet);

            // Feed to relay manager
            relayManager.handle(packet);

            // Handle the tracker post-processing.
            trackerManager.handlePostPacket(packet);
        });
    }

    public <T extends Tracker> T  getTracker(Class<T> klass) {
        return trackerManager.getTrackerMap().getInstance(klass);
    }
}

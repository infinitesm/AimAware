package ai.aimaware.profile.manager;

import ai.aimaware.AimAwarePlugin;
import ai.aimaware.profile.model.Profile;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Manages Profile objects for online players.
 */
public class ProfileManager implements Listener {

    private final Map<UUID, Profile> playerProfiles = Maps.newConcurrentMap();
    private final AimAwarePlugin plugin;

    public ProfileManager(AimAwarePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the Profile for the given player,
     * creating it if it doesn't already exist.
     *
     * @param player The Bukkit player.
     * @return The associated Profile object.
     */
    public Profile get(Player player) {
        return playerProfiles.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new Profile(player, plugin)
        );
    }

    /**
     * Removes the Profile for the given player.
     *
     * @param player The Bukkit player.
     */
    public void remove(Player player) {
        playerProfiles.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        get(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }
}

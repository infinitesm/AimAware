package ai.visient;

import ai.visient.profile.manager.ProfileManager;
import ai.visient.util.AsyncJsonClient;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class VisientPlugin extends JavaPlugin implements Listener {

    private ProfileManager playerProfileManager;
    private AsyncJsonClient asyncJsonClient;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Visient.setPlugin(this);

        playerProfileManager = new ProfileManager();

        Bukkit.getPluginManager().registerEvents(this, this);

        asyncJsonClient = new AsyncJsonClient();
        asyncJsonClient.open();
    }

    @Override
    public void onDisable() {
        asyncJsonClient.close();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        playerProfileManager.get(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        playerProfileManager.remove(event.getPlayer());
    }


}

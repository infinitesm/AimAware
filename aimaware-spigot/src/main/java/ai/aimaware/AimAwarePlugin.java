package ai.aimaware;

import ai.aimaware.command.manager.CommandManager;
import ai.aimaware.network.client.AsyncJsonClient;
import ai.aimaware.profile.manager.ProfileManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class AimAwarePlugin extends JavaPlugin {

    private static final String INFERENCE_URL = "http://127.0.0.1:8000/predict";
    private static final String COLLECTION_URL = "http://127.0.0.1:8000/collect";

    @Getter
    private ProfileManager profileManager;

    @Getter
    private AsyncJsonClient asyncJsonClient;

    @Override
    public void onEnable() {
        // Instantiate core dependencies
        this.profileManager = new ProfileManager(this);

        CommandManager commandManager = new CommandManager(profileManager);
        commandManager.init();

        this.asyncJsonClient = new AsyncJsonClient(INFERENCE_URL, COLLECTION_URL);
        this.asyncJsonClient.open();

        // Register listeners and inject dependencies
        this.getServer().getPluginManager().registerEvents(profileManager, this);

        // Register main command for command manager
        this.getCommand("aimaware").setExecutor(commandManager);

        this.getLogger().info("AimAware enabled successfully.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("AimAware disabled successfully.");

        // Close open client connection.
        asyncJsonClient.close();
    }
}

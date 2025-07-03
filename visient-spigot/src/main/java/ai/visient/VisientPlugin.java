package ai.visient;

import ai.visient.command.manager.CommandManager;
import ai.visient.network.client.AsyncJsonClient;
import ai.visient.profile.manager.ProfileManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class VisientPlugin extends JavaPlugin {

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
        this.getCommand("visient").setExecutor(commandManager);

        this.getLogger().info("Visient enabled successfully.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Visient disabled successfully.");

        // Close open client connection.
        asyncJsonClient.close();
    }
}

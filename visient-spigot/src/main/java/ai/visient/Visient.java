package ai.visient;

import ai.visient.profile.manager.ProfileManager;
import ai.visient.util.AsyncJsonClient;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Visient {
    @Getter
    private VisientPlugin plugin;

    public void setPlugin(VisientPlugin plugin) {
        if (Visient.plugin != null) {
            throw new UnsupportedOperationException("Plugin is already defined.");
        }

        Visient.plugin = plugin;
    }

    public ProfileManager getPlayerDataManager() {
        return plugin.getPlayerProfileManager();
    }

    public AsyncJsonClient getAsyncJsonClient() { return plugin.getAsyncJsonClient(); }
}

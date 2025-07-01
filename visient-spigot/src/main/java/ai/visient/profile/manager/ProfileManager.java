package ai.visient.profile.manager;

import ai.visient.profile.Profile;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ProfileManager {
    private final Map<UUID, Profile> playerProfiles = Maps.newConcurrentMap();

    public Profile get(Player player) {
        UUID uuid = player.getUniqueId();
        Profile profile = playerProfiles.get(uuid);
        if (profile == null) playerProfiles.put(uuid, profile = new Profile(player));
        return profile;
    }

    public void remove(Player player) {
        playerProfiles.remove(player.getUniqueId());

    }
}
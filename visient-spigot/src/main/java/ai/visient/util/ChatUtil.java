package ai.visient.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

@UtilityClass
public class ChatUtil {
    public void debug(String debug, Object... args) {
        debug = String.format(debug, args);
        debug = ChatColor.translateAlternateColorCodes('&', debug);

        Bukkit.broadcastMessage(debug);
    }
}

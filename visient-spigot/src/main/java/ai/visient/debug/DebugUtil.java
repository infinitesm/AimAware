package ai.visient.debug;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@UtilityClass
public class DebugUtil {
    // It's not the best looking thing, but it will work for a prototype.
    public void debugInference(String name, String prediction, double confidence) {
        String format = "&aV &7> &fInference for %s > %s, &cconfidence: %.4f ";
        format = String.format(format, name, prediction, confidence);
        format = ChatColor.translateAlternateColorCodes('&', format);
        Bukkit.broadcastMessage(format);
    }

    public void broadcast(String message, Object... args) {
        message = String.format(message, args);
        message = ChatColor.translateAlternateColorCodes('&', message);

        Bukkit.broadcastMessage(message);
    }
}

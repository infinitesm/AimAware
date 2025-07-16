package ai.aimaware.debug;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@UtilityClass
public class DebugUtil {
    // TODO: Clean this up, this class shouldn't be necessary.
    public void debugInference(String name, String prediction, double confidence, double threshold) {
        String format = "&d[AimAware] &e%s &d> &e%s &d(C: %.2f%%, T: %.2f)";
        format = String.format(format, name, prediction, confidence * 100.0, threshold);
        format = ChatColor.translateAlternateColorCodes('&', format);
        Bukkit.broadcastMessage(format);
    }

    public void broadcast(String message, Object... args) {
        message = String.format(message, args);
        message = ChatColor.translateAlternateColorCodes('&', message);

        Bukkit.broadcastMessage(message);
    }
}

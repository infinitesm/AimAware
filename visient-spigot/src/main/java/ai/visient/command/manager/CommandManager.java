package ai.visient.command.manager;

import ai.visient.command.base.CommandHandler;
import ai.visient.command.impl.ModeCommand;
import ai.visient.profile.manager.ProfileManager;
import ai.visient.profile.model.Profile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    private final Map<String, CommandHandler> subcommands = new HashMap<>();
    private final ProfileManager profileManager;

    public CommandManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    /**
     * Initialize the command manager, registering all sub commands.
     */
    public void init() {
        register("mode", new ModeCommand());
    }

    /**
     * Register a subcommand by name.
     *
     * @param name     subcommand name
     * @param handler  the handler to run
     */
    private void register(String name, CommandHandler handler) {
        subcommands.put(name.toLowerCase(), handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        Profile profile = profileManager.get(player);

        if (profile == null) {
            player.sendMessage("Profile irretrievable. Please relog.");
            return true;
        }

        if (args.length == 0) {
            profile.message("&cUsage: /" + label + " <subcommand>");
            return true;
        }

        String sub = args[0].toLowerCase();
        CommandHandler handler = subcommands.get(sub);

        if (handler == null) {
            profile.message("&cUnknown subcommand: " + sub);
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        if (args.length > 1) {
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        }

        handler.execute(profile, Arrays.asList(subArgs));
        return true;
    }
}

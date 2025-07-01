package ai.visient.command.impl;

import ai.visient.command.base.CommandHandler;
import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.impl.info.InfoTracker;
import ai.visient.profile.tracker.impl.info.enums.RelayMode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ModeCommand implements CommandHandler {

    @Override
    public void execute(Profile profile, List<String> args) {
        if (args.size() != 1) {
            profile.getPlayer().sendMessage("§cUsage: /visient mode <inference|collection>");
            return;
        }

        String input = args.get(0).toUpperCase();

        Optional<RelayMode> relayMode = Arrays.stream(RelayMode.values())
                .filter(r -> r.name().equalsIgnoreCase(input))
                .findFirst();

        if (relayMode.isPresent()) {
            InfoTracker tracker = profile.getTracker(InfoTracker.class);
            tracker.setRelayMode(relayMode.get());
            profile.getPlayer().sendMessage("§aRelay mode set to: " + relayMode.get().name());
        } else {
            profile.getPlayer().sendMessage("§cInvalid relay mode!");
        }
    }
}

package ai.visient.command.impl;

import ai.visient.command.base.CommandHandler;
import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.impl.info.InfoTracker;
import ai.visient.profile.tracker.impl.info.enums.RelayMode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Sub command used to swap between inference and data collection mode
 */
public class ModeCommand implements CommandHandler {

    @Override
    public void execute(Profile profile, List<String> args) {
        if (args.isEmpty()) {
            profile.message("&cUsage: /visient mode <inference|collection <configName>>");
            return;
        }

        String input = args.get(0).toUpperCase();

        Optional<RelayMode> relayMode = Arrays.stream(RelayMode.values())
                .filter(r -> r.name().equalsIgnoreCase(input))
                .findFirst();

        if (relayMode.isPresent()) {
            InfoTracker tracker = profile.getTracker(InfoTracker.class);
            tracker.setRelayMode(relayMode.get());
            profile.message("&aRelay mode set to: " + relayMode.get().name());

            if (relayMode.get() == RelayMode.COLLECTION) {
                String config = args.get(1);
                tracker.setCollectionConfig(config);

                profile.message("&aCollection config set to: " + config);
            }
        } else {
            profile.getPlayer().sendMessage("&cInvalid relay mode!");
        }
    }
}

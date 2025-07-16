package ai.aimaware.command.base;

import ai.aimaware.profile.model.Profile;

import java.util.List;

public interface CommandHandler {
    void execute(Profile profile, List<String> args);
}

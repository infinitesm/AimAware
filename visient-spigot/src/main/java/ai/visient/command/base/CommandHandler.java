package ai.visient.command.base;

import ai.visient.profile.model.Profile;

import java.util.List;

public interface CommandHandler {
    void execute(Profile profile, List<String> args);
}

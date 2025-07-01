package ai.visient.profile.tracker.impl.info;

import ai.visient.profile.model.Profile;
import ai.visient.profile.tracker.base.Tracker;
import ai.visient.profile.tracker.impl.info.enums.RelayMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfoTracker extends Tracker {

    public InfoTracker(Profile profile) {
        super(profile);
    }

    private RelayMode relayMode = RelayMode.NONE;
}

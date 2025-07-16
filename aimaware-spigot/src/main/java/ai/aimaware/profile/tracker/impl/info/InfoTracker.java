package ai.aimaware.profile.tracker.impl.info;

import ai.aimaware.profile.model.Profile;
import ai.aimaware.profile.tracker.base.Tracker;
import ai.aimaware.profile.tracker.impl.info.enums.RelayMode;
import lombok.Getter;
import lombok.Setter;

/**
 * Holder for various crucial information related to the player.
 */
@Getter
@Setter
public class InfoTracker extends Tracker {

    public InfoTracker(Profile profile) {
        super(profile);
    }

    private RelayMode relayMode = RelayMode.NONE;
    private String collectionConfig = "";
}

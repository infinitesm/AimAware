package ai.visient.profile.tracker.impl.info.enums;

/**
 * Relay status for the Visient server.
 * COLLECTION --> Used to collect data into the dataset
 * INFERENCE --> Used for live player detection
 */
public enum RelayMode {
    COLLECTION, INFERENCE, NONE
}

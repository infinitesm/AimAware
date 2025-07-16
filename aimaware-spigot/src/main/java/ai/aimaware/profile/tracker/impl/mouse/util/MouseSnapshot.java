package ai.aimaware.profile.tracker.impl.mouse.util;

import ai.aimaware.profile.tracker.impl.entity.util.BoundingBox;
import lombok.Builder;
import lombok.Getter;

/**
 * Holder for information relevant to how a player aimed, especially in relation to a current target.
 */
@Getter
@Builder
public class MouseSnapshot {
    private final BoundingBox targetBox;
    private final double interceptX;
    private final double interceptY;
    private final float accelerationYaw;
    private final float accelerationPitch;
    private final float deltaYaw;
    private final float deltaPitch;
    private final double offsetFromCenter;
}
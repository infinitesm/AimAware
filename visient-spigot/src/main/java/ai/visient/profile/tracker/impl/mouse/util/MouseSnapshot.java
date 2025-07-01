package ai.visient.profile.tracker.impl.mouse.util;

import ai.visient.profile.tracker.impl.entity.util.BoundingBox;
import lombok.Builder;
import lombok.Getter;

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
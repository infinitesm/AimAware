package ai.visient.util;

import ai.visient.profile.Profile;
import ai.visient.profile.tracker.impl.PositionTracker;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReachUtil {
    public float[] getRotations(double[] cameraPosition, double[] vertex) {
        double dx = vertex[0] - cameraPosition[0];
        double dy = vertex[1] - cameraPosition[1];
        double dz = vertex[2] - cameraPosition[2];

        double dist = Math.hypot(dx, dz);

        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, dist) * 180.0 / Math.PI));

        yaw = (yaw % 360f + 360f) % 360f;

        return new float[]{yaw, pitch};
    }

    public double[][] obtainCameraPositions(Profile profile) {
        PositionTracker positionTracker = profile.getTracker(PositionTracker.class);

        return new double[][]{
                {positionTracker.getTo().getX(), positionTracker.getTo().getY() + 1.62, positionTracker.getTo().getZ()},
                {positionTracker.getTo().getX(), positionTracker.getTo().getY() + 1.54, positionTracker.getTo().getZ()}
        };
    }
}

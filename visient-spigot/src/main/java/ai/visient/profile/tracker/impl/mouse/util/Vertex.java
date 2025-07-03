package ai.visient.profile.tracker.impl.mouse.util;

import lombok.Getter;

/**
 * 3D point in Euclidean space.
 */
@Getter
public class Vertex {
    private final double x, y, z;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double[] toArray() {
        return new double[]{x, y, z};
    }
}

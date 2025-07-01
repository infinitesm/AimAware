package ai.visient.profile.tracker.impl.entity.util;

import lombok.Getter;

@Getter
public class BoundingBox {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public final long timestamp = System.currentTimeMillis();

    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(maxX, minX);
        this.maxY = Math.max(maxY, minY);
        this.maxZ = Math.max(maxZ, minZ);
    }

    public BoundingBox expand(double x, double y, double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;

        return this;
    }

    public BoundingBox contain(BoundingBox... cubes) {
        for (BoundingBox cube : cubes) {
            this.minX = Math.min(this.minX, cube.minX);
            this.minY = Math.min(this.minY, cube.minY);
            this.minZ = Math.min(this.minZ, cube.minZ);

            this.maxX = Math.max(this.maxX, cube.maxX);
            this.maxY = Math.max(this.maxY, cube.maxY);
            this.maxZ = Math.max(this.maxZ, cube.maxZ);
        }

        return this;
    }


    public BoundingBox copy() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public double posX() {
        return (maxX + minX) / 2.0;
    }

    public double posZ() {
        return (maxZ + minZ) / 2.0;
    }

    public void set(BoundingBox copy) {
        minX = copy.minX;
        minY = copy.minY;
        minZ = copy.minZ;
        maxX = copy.maxX;
        maxY = copy.maxY;
        maxZ = copy.maxZ;
    }

    public void set(double minX, double minY, double minZ,
                    double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
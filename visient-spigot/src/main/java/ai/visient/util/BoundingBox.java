package ai.visient.util;

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

    public BoundingBox(double x, double y, double z, float width, float height) {
        this.minX = x - (width / 2.0F);
        this.minY = y;
        this.minZ = z - (width / 2.0F);
        this.maxX = x + (width / 2.0F);
        this.maxY = y + height;
        this.maxZ = z + (width / 2.0F);
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

    public static BoundingBox fromPlayerLocation(PlayerLocation location) {
        return new BoundingBox(location.getX() - 0.3F, location.getY(), location.getZ() + 0.3F,
                location.getX() + 0.3F, location.getY() + 1.8F, location.getZ() + 0.3F);
    }

    public BoundingBox addCoord(double x, double y, double z)
    {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D)
        {
            d0 += x;
        }
        else if (x > 0.0D)
        {
            d3 += x;
        }

        if (y < 0.0D)
        {
            d1 += y;
        }
        else if (y > 0.0D)
        {
            d4 += y;
        }

        if (z < 0.0D)
        {
            d2 += z;
        }
        else if (z > 0.0D)
        {
            d5 += z;
        }

        return new BoundingBox(d0, d1, d2, d3, d4, d5);
    }


    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateZOffset(BoundingBox other, double offsetZ)
    {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY)
        {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ)
            {
                double d1 = this.minZ - other.maxZ;

                if (d1 < offsetZ)
                {
                    offsetZ = d1;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= this.maxZ)
            {
                double d0 = this.maxZ - other.minZ;

                if (d0 > offsetZ)
                {
                    offsetZ = d0;
                }
            }

            return offsetZ;
        }
        else
        {
            return offsetZ;
        }
    }

    public double calculateXOffset(BoundingBox other, double offsetX)
    {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ)
        {
            if (offsetX > 0.0D && other.maxX <= this.minX)
            {
                double d1 = this.minX - other.maxX;

                if (d1 < offsetX)
                {
                    offsetX = d1;
                }
            }
            else if (offsetX < 0.0D && other.minX >= this.maxX)
            {
                double d0 = this.maxX - other.minX;

                if (d0 > offsetX)
                {
                    offsetX = d0;
                }
            }

            return offsetX;
        }
        else
        {
            return offsetX;
        }
    }

    public BoundingBox offset(double x, double y, double z)
    {
        return new BoundingBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public boolean collides(BoundingBox other) {
        return other.maxX >= this.minX
                && other.minX <= this.maxX
                && other.maxY >= this.minY
                && other.minY <= this.maxY
                && other.maxZ >= this.minZ
                && other.minZ <= this.maxZ;
    }

    public boolean collidesUnder(BoundingBox other) {
        return maxY == other.minY
                && minZ < other.maxZ
                && minX < other.maxX
                && maxZ > other.minZ
                && maxX > other.minX;
    }

    public boolean collidesAbove(BoundingBox other) {
        return minY == other.maxY
                && minZ < other.maxZ
                && minX < other.maxX
                && maxZ > other.minZ
                && maxX > other.minX;
    }

    public boolean collidesHorizontally(BoundingBox other) {
        boolean vertical = maxY > other.minY && minY < other.maxY;
        boolean horizontal = minX == other.maxX || maxX == other.minX || minZ == other.maxZ || maxZ == other.minZ;

        return vertical && horizontal;
    }

    public BoundingBox grow(double x, double y, double z) {
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

    public BoundingBox move(double x, double y, double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;

        return this;
    }

    public BoundingBox copy() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public double posX() {
        return (maxX + minX) / 2.0;
    }

    public double posY() {
        return minY;
    }

    public double posZ() {
        return (maxZ + minZ) / 2.0;
    }

    public double calculateYOffset(BoundingBox other, double offsetY)
    {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ)
        {
            if (offsetY > 0.0D && other.maxY <= this.minY)
            {
                double d1 = this.minY - other.maxY;

                if (d1 < offsetY)
                {
                    offsetY = d1;
                }
            }
            else if (offsetY < 0.0D && other.minY >= this.maxY)
            {
                double d0 = this.maxY - other.minY;

                if (d0 > offsetY)
                {
                    offsetY = d0;
                }
            }

            return offsetY;
        }
        else
        {
            return offsetY;
        }
    }

    public BoundingBox set(double x, double y, double z, double width, double height) {
        this.minX = x - width / 2.0;
        this.minY = y;
        this.minZ = z - width / 2.0;

        this.maxX = x + width / 2.0;
        this.maxY = y + height;
        this.maxZ = z + width / 2.0;

        return this;
    }

    public BoundingBox set(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(maxX, minX);
        this.maxY = Math.max(maxY, minY);
        this.maxZ = Math.max(maxZ, minZ);

        return this;
    }

    public BoundingBox set(BoundingBox boundingBox) {
        this.minX = Math.min(boundingBox.minX, boundingBox.maxX);
        this.minY = Math.min(boundingBox.minY, boundingBox.maxY);
        this.minZ = Math.min(boundingBox.minZ, boundingBox.maxZ);
        this.maxX = Math.max(boundingBox.maxX, boundingBox.minX);
        this.maxY = Math.max(boundingBox.maxY, boundingBox.minY);
        this.maxZ = Math.max(boundingBox.maxZ, boundingBox.minZ);

        return this;
    }
}
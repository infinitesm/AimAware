package ai.aimaware.profile.tracker.impl.entity.util;

/**
 * Tracks an entity’s bounding box based on compressed server coordinates,
 * and interpolates bounds between packets for smooth detection.
 */
public class TrackedEntity {

    public BoundingBox area;
    public BoundingBox potential;

    public final int id;
    public final float width;
    public final float height;

    public int interpolationTicks;

    // Server compressed positions
    public int compressedX;
    public int compressedY;
    public int compressedZ;

    /**
     * Constructs the tracked entity at the specified compressed server coordinates.
     *
     * @param id the entity’s NMS ID
     * @param x compressed X
     * @param y compressed Y
     * @param z compressed Z
     * @param width entity width in world units
     * @param height entity height in world units
     */
    public TrackedEntity(int id, int x, int y, int z, float width, float height) {
        this.id = id;
        this.width = width / 2.0f;
        this.height = height;
        updateCompressedPosition(x, y, z);

        // Initial bounds created from starting position
        this.area = getPotentialPoint();
    }

    /**
     * Updates the entity’s bounds with a new compressed server position.
     * Expands the current bounds to contain the new position box.
     *
     * @param x compressed X
     * @param y compressed Y
     * @param z compressed Z
     */
    public void updateBounds(int x, int y, int z) {
        updateCompressedPosition(x, y, z);
        BoundingBox potential = getPotentialPoint();

        area.contain(potential);
        interpolationTicks = 3;
    }

    /**
     * Interpolates the bounds closer to the server's known position
     * for smooth transitions between packets.
     */
    public void interpolate() {
        if (interpolationTicks <= 0) return;

        double posX = compressedX / 32.0d;
        double posY = compressedY / 32.0d;
        double posZ = compressedZ / 32.0d;

        double minX = area.getMinX() + (posX - area.getMinX()) / interpolationTicks;
        double minY = area.getMinY() + (posY - area.getMinY()) / interpolationTicks;
        double minZ = area.getMinZ() + (posZ - area.getMinZ()) / interpolationTicks;

        double maxX = area.getMaxX() + (posX - area.getMaxX()) / interpolationTicks;
        double maxY = area.getMaxY() + (posY - area.getMaxY()) / interpolationTicks;
        double maxZ = area.getMaxZ() + (posZ - area.getMaxZ()) / interpolationTicks;

        this.area.set(minX, minY, minZ, maxX, maxY, maxZ);

        interpolationTicks--;
    }

    /**
     * Returns a bounding box expanded to account for the 0.1 block expansion
     * that Minecraft applies to hitboxes internally.
     *
     * This 0.1 value comes from reverse-engineering Mojang’s source.
     */
    public BoundingBox revealBounds() {
        float mojangExpansion = 0.1f;

        BoundingBox revealed = new BoundingBox(
                area.getMinX() - width,
                area.getMinY(),
                area.getMinZ() - width,
                area.getMaxX() + width,
                area.getMaxY() + height,
                area.getMaxZ() + width
        );

        revealed.expand(mojangExpansion, mojangExpansion, mojangExpansion);
        return revealed;
    }

    /**
     * Updates the stored compressed server position.
     * This step is crucial for correct tracking.
     */
    private void updateCompressedPosition(int x, int y, int z) {
        this.compressedX = x;
        this.compressedY = y;
        this.compressedZ = z;
    }

    /**
     * Creates a 1-point bounding box based on the server’s compressed position.
     */
    private BoundingBox getPotentialPoint() {
        double posX = compressedX / 32.0d;
        double posY = compressedY / 32.0d;
        double posZ = compressedZ / 32.0d;

        return new BoundingBox(posX, posY, posZ, posX, posY, posZ);
    }
}

package ai.visient.util;

public class TrackedEntity {

    public BoundingBox bounds, lastBounds;
    public final int entityId;
    public final float width, height;

    public BoundingBox confirmingBounds;
    public int interpolationTicks;
    public int serverPosX;
    public int serverPosY;
    public int serverPosZ;

    public TrackedEntity(int entityId, int x, int y, int z, float width, float height) {
        this.entityId = entityId;
        this.width = width / 2.0f;
        this.height = height;

        // Create the bounds based on the compressed position.
        this.bounds = bind(x, y, z);
        this.lastBounds = bounds;
    }

    public void updateBounds(int x, int y, int z) {
        // Create a bounding box from the compressed position.
        BoundingBox positionBox = bind(x, y, z);

        // Wrap the current bounds around the created bounds.
        this.bounds.contain(positionBox);

        // Update the interpolation ticks.
        interpolationTicks = 3;
    }

    public void interpolate() {
        if (interpolationTicks > 0) {
            // Create the uncompressed position.
            double posX = serverPosX / 32.0d;
            double posY = serverPosY / 32.0d;
            double posZ = serverPosZ / 32.0d;

            // Get the absolute bounds the position cannot be out of.
            double minX = bounds.getMinX();
            double minY = bounds.getMinY();
            double minZ = bounds.getMinZ();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            double maxZ = bounds.getMaxZ();

            // Do the interpolation.
            minX += (posX - minX) / interpolationTicks;
            minY += (posY - minY) / interpolationTicks;
            minZ += (posZ - minZ) / interpolationTicks;
            maxX += (posX - maxX) / interpolationTicks;
            maxY += (posY - maxY) / interpolationTicks;
            maxZ += (posZ - maxZ) / interpolationTicks;

            // Set the new bounds.
            this.lastBounds.set(this.bounds.copy());
            this.bounds.set(minX, minY, minZ, maxX, maxY, maxZ);

            // Decrement the ticks for interpolation.
            --interpolationTicks;
        }
    }

    public BoundingBox revealBounds() {
        float expansion = 0.1f;

        BoundingBox revealedBounds = new BoundingBox(
                bounds.minX - width,
                bounds.minY,
                bounds.minZ - width,
                bounds.maxX + width,
                bounds.maxY + height,
                bounds.maxZ + width
        );

        revealedBounds.grow(expansion, expansion, expansion);

        return revealedBounds;
    }

    public BoundingBox revealLastBounds() {
        float expansion = 0.1f;

        BoundingBox revealedBounds = new BoundingBox(
                lastBounds.minX - width,
                lastBounds.minY,
                lastBounds.minZ - width,
                lastBounds.maxX + width,
                lastBounds.maxY + height,
                lastBounds.maxZ + width
        );

        revealedBounds.grow(expansion, expansion, expansion);

        return revealedBounds;
    }

    private BoundingBox bind(int x, int y, int z) {
        // Decompress the position.
        double posX = (serverPosX = x) / 32.0d;
        double posY = (serverPosY = y) / 32.0d;
        double posZ = (serverPosZ = z) / 32.0d;

        return new BoundingBox(posX, posY, posZ, posX, posY, posZ);
    }

}
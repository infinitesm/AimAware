package ai.visient.util;

import lombok.Getter;

@Getter
public class MouseSnapshot {
    private final BoundingBox targetBox;
    private final double interceptX;
    private final double interceptY;
    private final double accelerationYaw;
    private final double accelerationPitch;
    private final double deltaYaw;
    private final double deltaPitch;
    private final double offsetFromCenter;

    private MouseSnapshot(Builder builder) {
        this.targetBox = builder.targetBox;
        this.interceptX = builder.interceptX;
        this.interceptY = builder.interceptY;
        this.deltaYaw = builder.deltaYaw;
        this.deltaPitch = builder.deltaPitch;
        this.accelerationYaw = builder.accelerationYaw;
        this.accelerationPitch = builder.accelerationPitch;
        this.offsetFromCenter = builder.offsetFromCenter;
    }
    public boolean isInterceptingEntity() {
        return MathUtil.checkRange(-0.1, 1.1, interceptX)
                && MathUtil.checkRange(-0.1, 1.1, interceptY);
    }

    public static class Builder {

        private BoundingBox targetBox;
        private double interceptX;
        private double interceptY;
        private double accelerationYaw;
        private double accelerationPitch;
        private double deltaYaw;
        private double deltaPitch;
        private double offsetFromCenter;
        public Builder() {}

        public Builder setTargetBox(BoundingBox targetBox) {
            this.targetBox = targetBox;
            return this;
        }
        public Builder setInterceptX(double interceptX) {
            this.interceptX = interceptX;
            return this;
        }

        public Builder setInterceptY(double interceptY) {
            this.interceptY = interceptY;
            return this;
        }

        public Builder setAccelerationYaw(double accelerationYaw) {
            this.accelerationYaw = accelerationYaw;
            return this;
        }

        public Builder setAccelerationPitch(double accelerationPitch) {
            this.accelerationPitch = accelerationPitch;
            return this;
        }

        public Builder setDeltaYaw(double deltaYaw) {
            this.deltaYaw = deltaYaw;
            return this;
        }

        public Builder setDeltaPitch(double deltaPitch) {
            this.deltaPitch = deltaPitch;
            return this;
        }

        public Builder setOffsetFromCenter(double offsetFromCenter) {
            this.offsetFromCenter = offsetFromCenter;
            return this;
        }


        public MouseSnapshot build() {
            return new MouseSnapshot(this);
        }
    }
}
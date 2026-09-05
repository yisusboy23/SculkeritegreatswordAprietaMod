package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;

import java.util.EnumSet;

public class ResonarchHoverWanderGoal extends Goal {

    private final ResonarchEntity resonarch;

    private double targetX;
    private double targetY;
    private double targetZ;

    private int cooldown;

    public ResonarchHoverWanderGoal(ResonarchEntity resonarch) {
        this.resonarch = resonarch;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {

        if (this.resonarch.isExclusiveActionActive()) {
            return false;
        }

        if (this.resonarch.getTarget() != null) {
            return false;
        }

        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        Vec3 target = findHoverPosition();

        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;

        return true;
    }

    @Override
    public boolean canContinueToUse() {

        if (this.resonarch.isExclusiveActionActive()) {
            return false;
        }

        if (this.resonarch.getTarget() != null) {
            return false;
        }

        double dx = this.targetX - this.resonarch.getX();
        double dy = this.targetY - this.resonarch.getY();
        double dz = this.targetZ - this.resonarch.getZ();

        return dx * dx + dy * dy + dz * dz > 0.25D;
    }

    @Override
    public void start() {
        setMoveTarget();
    }

    @Override
    public void tick() {
        setMoveTarget();
    }

    @Override
    public void stop() {

        this.resonarch.setDeltaMovement(
                this.resonarch.getDeltaMovement().scale(0.7D)
        );

        this.cooldown = 20 + this.resonarch.getRandom().nextInt(21);
    }

    private void setMoveTarget() {

        this.resonarch.getMoveControl().setWantedPosition(
                this.targetX,
                this.targetY,
                this.targetZ,
                1.0D
        );
    }

    private Vec3 findHoverPosition() {

        Vec3 current = this.resonarch.position();

        double x =
                current.x +
                        (this.resonarch.getRandom().nextDouble() - 0.5D) * 12.0D;

        double y =
                current.y +
                        (this.resonarch.getRandom().nextDouble() - 0.5D) * 4.0D;

        double z =
                current.z +
                        (this.resonarch.getRandom().nextDouble() - 0.5D) * 12.0D;

        return new Vec3(x, y, z);
    }
}
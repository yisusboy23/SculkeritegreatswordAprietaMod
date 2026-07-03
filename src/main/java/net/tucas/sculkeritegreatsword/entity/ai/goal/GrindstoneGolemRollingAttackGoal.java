package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.GrindstoneGolemEntity;

import java.util.EnumSet;

public class GrindstoneGolemRollingAttackGoal extends Goal {

    private final GrindstoneGolemEntity golem;
    private final double maxRange;
    private final double minRange;
    private LivingEntity target;

    public GrindstoneGolemRollingAttackGoal(GrindstoneGolemEntity golem, double maxRange, double minRange) {
        this.golem = golem;
        this.maxRange = maxRange;
        this.minRange = minRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = this.golem.getTarget();

        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        if (!this.golem.canUseRolling()) {
            return false;
        }

        double dist = this.golem.distanceTo(this.target);
        return dist >= this.minRange && dist <= this.maxRange;
    }

    @Override
    public boolean canContinueToUse() {
        return this.golem.isRolling();
    }

    @Override
    public void start() {
        this.golem.startRollingAttack(this.target);
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (this.target != null) {
            this.golem.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }
    }
}
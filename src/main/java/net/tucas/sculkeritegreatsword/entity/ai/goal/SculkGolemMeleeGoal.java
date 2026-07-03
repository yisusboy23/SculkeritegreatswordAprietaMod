package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;

import java.util.EnumSet;

public class SculkGolemMeleeGoal extends Goal {

    private final SculkGolemEntity golem;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;

    public SculkGolemMeleeGoal(SculkGolemEntity golem, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.golem = golem;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }

        if (this.golem.isUsingSonicBoom()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.golem.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }

        if (this.golem.isUsingSonicBoom()) {
            return false;
        }

        if (!this.followingTargetEvenIfNotSeen) {
            return !this.golem.getNavigation().isDone();
        }

        return this.golem.isWithinRestriction(target.blockPosition());
    }

    @Override
    public void start() {
        this.golem.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        LivingEntity target = this.golem.getTarget();

        // FIX: Verificar null ANTES de usar canAttack
        if (target != null && !this.golem.canAttack(target)) {
            this.golem.setTarget(null);
        }

        this.golem.setAggressive(false);
        this.golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.golem.getTarget();
        if (target == null) {
            return;
        }

        this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

        if (this.ticksUntilNextPathRecalculation <= 0) {
            this.ticksUntilNextPathRecalculation = 4 + this.golem.getRandom().nextInt(7);
            this.golem.getNavigation().moveTo(target, this.speedModifier);
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);

        double distSqr = this.golem.distanceToSqr(target.getX(), target.getY(), target.getZ());
        double attackReachSqr = this.getAttackReachSqr(target);

        if (distSqr <= attackReachSqr && this.ticksUntilNextAttack <= 0) {
            this.resetAttackCooldown();
            this.golem.startAttackSequence(target);
        }
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = 20;
    }

    protected double getAttackReachSqr(LivingEntity target) {
        return this.golem.getBbWidth() * 2.0F * this.golem.getBbWidth() * 2.0F + target.getBbWidth();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
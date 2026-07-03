package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;

import java.util.EnumSet;

public class OxicopperGolemFleeFromEnemyGoal extends Goal {

    private final OxicopperGolemEntity golem;
    private final double speedModifier;
    private final float minDistance;
    private LivingEntity target;
    private Vec3 fleePos;

    private static final int MIN_HITS_TO_FLEE = 4;

    public OxicopperGolemFleeFromEnemyGoal(OxicopperGolemEntity golem, double speedModifier, float minDistance) {
        this.golem = golem;
        this.speedModifier = speedModifier;
        this.minDistance = minDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.golem.isInShell()) {
            return false;
        }

        this.target = this.golem.getTarget();

        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        if (this.golem.getConsecutiveHits() < MIN_HITS_TO_FLEE) {
            return false;
        }

        double distSqr = this.golem.distanceToSqr(this.target);

        if (distSqr < this.minDistance * this.minDistance) {
            this.fleePos = this.findFleePosition();
            return this.fleePos != null;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.golem.isInShell()) {
            return false;
        }

        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        double distSqr = this.golem.distanceToSqr(this.target);
        return distSqr < this.minDistance * this.minDistance &&
                this.golem.getConsecutiveHits() >= MIN_HITS_TO_FLEE &&
                !this.golem.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (this.fleePos != null) {
            this.golem.getNavigation().moveTo(this.fleePos.x, this.fleePos.y, this.fleePos.z, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        // CAMBIO CRÍTICO: Resetear el contador cuando termina de huir
        this.golem.resetConsecutiveHits();
        this.target = null;
        this.fleePos = null;
    }

    private Vec3 findFleePosition() {
        Vec3 awayFromEnemy = DefaultRandomPos.getPosAway(
                this.golem,
                16,
                7,
                this.target.position()
        );

        return awayFromEnemy;
    }
}
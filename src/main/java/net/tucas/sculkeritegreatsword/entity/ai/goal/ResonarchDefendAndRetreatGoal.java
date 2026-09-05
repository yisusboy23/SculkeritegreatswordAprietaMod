package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;

import java.util.EnumSet;

public class ResonarchDefendAndRetreatGoal extends Goal {

    private static final double TRIGGER_DISTANCE = 6.0D;
    private static final double SAFE_DISTANCE = 11.0D;
    private static final double PUSH_DISTANCE = 3.5D;
    private static final int MAX_FLEE_TICKS = 60;
    private static final int RETRIGGER_COOLDOWN_TICKS = 20;
    private static final int RECALC_INTERVAL_TICKS = 5;
    private static final int PUSH_INTERVAL_TICKS = 15;
    private static final double PUSH_STRENGTH = 0.6D;

    private final ResonarchEntity resonarch;
    private final double triggerDistanceSq = TRIGGER_DISTANCE * TRIGGER_DISTANCE;
    private final double safeDistanceSq = SAFE_DISTANCE * SAFE_DISTANCE;
    private final double pushDistanceSq = PUSH_DISTANCE * PUSH_DISTANCE;

    private Player target;
    private int fleeTicks;
    private int recalcCooldown;
    private int pushCooldown;
    private int retriggerCooldown;

    public ResonarchDefendAndRetreatGoal(ResonarchEntity resonarch) {
        this.resonarch = resonarch;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.retriggerCooldown > 0) {
            this.retriggerCooldown--;
            return false;
        }
        if (this.resonarch.isExclusiveActionActive()) {
            return false;
        }
        Player nearest = this.resonarch.level().getNearestPlayer(this.resonarch, 16.0D);
        return nearest != null && nearest.isAlive()
                && this.resonarch.distanceToSqr(nearest) < this.triggerDistanceSq;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.resonarch.isExclusiveActionActive()) {
            return false;
        }
        return this.fleeTicks < MAX_FLEE_TICKS
                && this.target != null && this.target.isAlive()
                && this.resonarch.distanceToSqr(this.target) < this.safeDistanceSq;
    }

    @Override
    public void start() {
        this.target = this.resonarch.level().getNearestPlayer(this.resonarch, 16.0D);
        if (this.target != null) {
            this.resonarch.setTarget(this.target);
        }
        this.fleeTicks = 0;
        this.recalcCooldown = 0;
        this.pushCooldown = 0;

        this.resonarch.setCombatState(ResonarchEntity.STATE_DEFEND);
        retreat();
    }

    @Override
    public void stop() {
        // FIX #1: Transición segura al terminar de huir
        if (!this.resonarch.isExclusiveActionActive()) {
            if (this.resonarch.hasFrozenProjectiles()) {
                this.resonarch.setCombatState(ResonarchEntity.STATE_DEFEND_IDLE);
            } else {
                this.resonarch.setCombatState(ResonarchEntity.STATE_IDLE);
            }
        }
        this.target = null;
        this.retriggerCooldown = RETRIGGER_COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        this.fleeTicks++;

        if (this.target != null) {
            this.resonarch.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }

        if (this.recalcCooldown-- <= 0) {
            this.recalcCooldown = RECALC_INTERVAL_TICKS;
            retreat();
        }
        if (this.target != null && this.pushCooldown-- <= 0
                && this.resonarch.distanceToSqr(this.target) < this.pushDistanceSq) {
            this.pushCooldown = PUSH_INTERVAL_TICKS;
            pushTargetAway();
        }
    }

    private void retreat() {
        if (this.target == null) return;

        Vec3 awayDir = this.resonarch.position().subtract(this.target.position()).normalize();
        Vec3 destination = this.resonarch.position().add(awayDir.scale(6.0D)).add(0, 0.5D, 0);
        this.resonarch.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 0.6D);

        Vec3 currentMovement = this.resonarch.getDeltaMovement();
        double speed = 0.25D;
        this.resonarch.setDeltaMovement(
                awayDir.x * speed,
                Math.max(currentMovement.y, 0.05D),
                awayDir.z * speed
        );
        this.resonarch.hasImpulse = true;
    }

    private void pushTargetAway() {
        Vec3 pushDir = this.target.position().subtract(this.resonarch.position()).normalize();
        this.target.setDeltaMovement(this.target.getDeltaMovement().add(pushDir.scale(PUSH_STRENGTH)).add(0, 0.15D, 0));
        this.target.hurtMarked = true;

        if (this.resonarch.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    this.resonarch.getX(), this.resonarch.getY() + 1.0D, this.resonarch.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.playSound(null, this.resonarch.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                    SoundSource.HOSTILE, 0.6F, 1.4F);
        }
    }
}
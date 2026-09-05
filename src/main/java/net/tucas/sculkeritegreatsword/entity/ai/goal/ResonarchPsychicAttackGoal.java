package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;

import java.util.EnumSet;

public class ResonarchPsychicAttackGoal extends Goal {

    private static final double RANGE = 20.0D;
    private static final float DAMAGE = 6.0F;

    private final ResonarchEntity boss;
    private LivingEntity target;

    public ResonarchPsychicAttackGoal(ResonarchEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity currentTarget = boss.getTarget();
        return currentTarget != null
                && currentTarget.isAlive()
                && boss.isReadyForPsychicAttack()
                && boss.distanceToSqr(currentTarget) <= RANGE * RANGE
                && boss.hasLineOfSight(currentTarget);
    }

    @Override
    public boolean canContinueToUse() {
        return boss.getCombatState() == ResonarchEntity.STATE_PSYCHIC_ATTACK
                && this.target != null
                && this.target.isAlive();
    }

    @Override
    public void start() {
        this.target = boss.getTarget();
        if (this.target == null) return;

        boss.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        boss.startPsychicAttackState();

        this.target.hurt(boss.damageSources().indirectMagic(boss, boss), DAMAGE);

        Vec3 push = this.target.position().subtract(boss.position()).normalize();
        this.target.setDeltaMovement(this.target.getDeltaMovement().add(push.x * 1.1D, 0.35D, push.z * 1.1D));
        this.target.hurtMarked = true;

        if (boss.level() instanceof ServerLevel serverLevel) {
            Vec3 targetEyePos = this.target.getEyePosition();
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                    targetEyePos.x, targetEyePos.y, targetEyePos.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.playSound(null, boss.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                    SoundSource.HOSTILE, 1.0F, 1.2F);
        }
    }

    @Override
    public void tick() {
        if (this.target != null) {
            this.boss.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }
    }

    @Override
    public void stop() {
        this.target = null;
    }
}
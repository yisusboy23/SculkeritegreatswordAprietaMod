package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

/** Priority 3 (anti-camping): rugido + overpower + atracción de jugadores lejanos. */
public class BossPullPlayersGoal extends AbstractBossGoal {
    private static final double PULL_RADIUS = 32.0D;
    private static final double PULL_MIN_DISTANCE = 6.0D;   // deja de tirar cuando ya están cerca
    private static final double PULL_STRENGTH = 0.55D;
    private static final int WINDUP_TICKS = 8;
    private static final int COOLDOWN = 200;
    private int ticks;

    public BossPullPlayersGoal(ForgottenConstructEntity boss) { super(boss); }

    @Override
    public boolean canUse() {
        BossState s = boss.getBossState();
        return boss.isAwake() && s != BossState.DAZED && s != BossState.WAKING_UP && s != BossState.SLEEP
                && hasValidTarget()
                && boss.getFarTicks() >= ForgottenConstructEntity.FAR_TICKS_FOR_PULL
                && boss.getPullCooldown() <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        return boss.getBossState() == BossState.PULLING && ticks < BossState.PULLING.getBaseTicks();
    }

    @Override
    public void start() {
        ticks = 0;
        boss.setBossState(BossState.PULLING);
        boss.playSound(SoundEvents.RAVAGER_ROAR, 4.0F, 0.7F);
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks < WINDUP_TICKS || !(boss.level() instanceof ServerLevel sl)) return;

        for (Player p : sl.getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(PULL_RADIUS),
                pl -> pl.isAlive() && !pl.isCreative() && !pl.isSpectator())) {
            Vec3 toBoss = new Vec3(boss.getX() - p.getX(), 0, boss.getZ() - p.getZ());
            double dist = toBoss.length();
            if (dist <= PULL_MIN_DISTANCE) continue;
            Vec3 dir = toBoss.normalize();
            p.setDeltaMovement(p.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D)
                    .add(dir.x * PULL_STRENGTH, p.onGround() ? 0.12D : 0.0D, dir.z * PULL_STRENGTH));
            p.hurtMarked = true;
            if (ticks % 4 == 0) sl.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1.0D, p.getZ(), 6, 0.3D, 0.5D, 0.3D, 0.1D);
        }
    }

    @Override
    public void stop() {
        returnToIdle();
        boss.setPullCooldown(COOLDOWN);
        boss.resetFarTicks();
    }
}

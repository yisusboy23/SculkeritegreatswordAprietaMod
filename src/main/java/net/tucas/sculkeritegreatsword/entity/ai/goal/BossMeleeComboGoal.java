package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

/** Priority 4: cuerpo a cuerpo (<= 4 bloques). Alterna Earthquake y Spin. */
public class BossMeleeComboGoal extends AbstractBossGoal {
    private static final int EARTHQUAKE_HIT_TICK = 15;
    private static final double EARTHQUAKE_RADIUS = 6.0D;
    private static final double SPIN_RADIUS = 5.0D;
    private static final int SPIN_HIT_INTERVAL = 10;   // respeta los i-frames de 10 ticks
    private static final int MELEE_LOCK = 80;          // cooldown forzado para que pase a rango

    private boolean earthquakeNext = true;
    private boolean finished;
    private int phaseTicks;

    public BossMeleeComboGoal(ForgottenConstructEntity boss) { super(boss); }

    @Override
    public boolean canUse() {
        if (!boss.isAwake() || boss.getBossState() != BossState.IDLE
                || boss.getMeleeCooldown() > 0 || !hasValidTarget()) return false;
        double r = ForgottenConstructEntity.MELEE_RANGE;
        return boss.distanceToSqr(boss.getTarget()) <= r * r;
    }

    @Override public boolean canContinueToUse() { return !finished && boss.getBossState().isMelee(); }

    @Override
    public void start() {
        finished = false;
        enter(earthquakeNext ? BossState.EARTHQUAKE : BossState.SPIN_START);
        earthquakeNext = !earthquakeNext;
    }

    private void enter(BossState s) { boss.setBossState(s); phaseTicks = 0; }

    @Override
    public void tick() {
        phaseTicks++;
        switch (boss.getBossState()) {
            case EARTHQUAKE -> {
                if (phaseTicks == boss.scaledTicks(EARTHQUAKE_HIT_TICK)) earthquakeSmash();
                if (phaseTicks >= boss.scaledTicks(BossState.EARTHQUAKE.getBaseTicks())) finished = true;
            }
            case SPIN_START -> {
                if (phaseTicks >= boss.scaledTicks(BossState.SPIN_START.getBaseTicks())) enter(BossState.SPIN);
            }
            case SPIN -> {
                if (phaseTicks % SPIN_HIT_INTERVAL == 0) spinHit();
                if (phaseTicks >= boss.scaledTicks(BossState.SPIN.getBaseTicks())) enter(BossState.SPIN_END);
            }
            case SPIN_END -> {
                if (phaseTicks >= boss.scaledTicks(BossState.SPIN_END.getBaseTicks())) finished = true;
            }
            default -> finished = true;
        }
    }

    private void earthquakeSmash() {
        float dmg = (float) boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        for (LivingEntity e : boss.getVictimsAround(EARTHQUAKE_RADIUS)) {
            boss.hurtAndPush(e, dmg, 2.2D, 0.7D);      // knockback fuerte
        }
        boss.playSound(SoundEvents.GENERIC_EXPLODE, 3.0F, 0.6F);
        if (boss.level() instanceof ServerLevel sl) {
            for (double r = 2.0D; r <= EARTHQUAKE_RADIUS; r += 2.0D) {
                for (int i = 0; i < 24; i++) {
                    double a = i * Math.PI / 12.0D;
                    sl.sendParticles(ParticleTypes.POOF, boss.getX() + Math.cos(a) * r, boss.getY() + 0.1D,
                            boss.getZ() + Math.sin(a) * r, 1, 0.1D, 0.05D, 0.1D, 0.03D);
                }
            }
        }
    }

    private void spinHit() {
        float dmg = (float) boss.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 0.5F;
        for (LivingEntity e : boss.getVictimsAround(SPIN_RADIUS)) {
            boss.hurtAndPush(e, dmg, 0.9D, 0.25D);     // daño continuo + empuje
        }
        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.5F);
    }

    @Override
    public void stop() {
        returnToIdle();
        boss.setMeleeCooldown(boss.scaledTicks(MELEE_LOCK));
    }
}

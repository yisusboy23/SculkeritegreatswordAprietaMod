package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

/**
 * Priority 2: aturdido 5 s (100 ticks) tras recibir su propia bola de fuego.
 * El cambio a DAZED lo hace la entidad en hurt(); aquí solo se gestiona el temporizador.
 * Mientras dura, la entidad deja de ser inmune a proyectiles y las capas emisivas se apagan.
 */
public class BossDazedGoal extends AbstractBossGoal {
    private int ticks;

    public BossDazedGoal(ForgottenConstructEntity boss) { super(boss); }

    @Override public boolean canUse() { return boss.getBossState() == BossState.DAZED; }

    @Override
    public boolean canContinueToUse() {
        return boss.getBossState() == BossState.DAZED && ticks < BossState.DAZED.getBaseTicks();
    }

    @Override public void start() { ticks = 0; }

    @Override
    public void tick() {
        ticks++;
        if (ticks % 6 == 0 && boss.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, boss.getX(), boss.getY() + boss.getBbHeight() + 0.3D, boss.getZ(),
                    4, 0.5D, 0.1D, 0.5D, 0.05D);
        }
    }

    @Override
    public void stop() {
        if (boss.getBossState() == BossState.DAZED) boss.setBossState(BossState.IDLE);
        boss.setRangedCooldown(40);
        boss.setMeleeCooldown(20);
    }
}

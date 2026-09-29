package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.sounds.SoundEvents;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

/** Priority 1: rugido de despertar (y de entrada a Fase 2). Al acabar enciende la capa glow. */
public class BossWakeUpGoal extends AbstractBossGoal {
    private int ticks;

    public BossWakeUpGoal(ForgottenConstructEntity boss) { super(boss); }

    @Override public boolean canUse() { return boss.getBossState() == BossState.WAKING_UP; }
    @Override public boolean canContinueToUse() { return boss.getBossState() == BossState.WAKING_UP; }

    @Override
    public void start() {
        ticks = 0;
        boss.playSound(SoundEvents.RAVAGER_ROAR, 4.0F, 0.5F);
    }

    @Override
    public void tick() {
        if (++ticks >= BossState.WAKING_UP.getBaseTicks()) {
            boss.setAwake(true);                       // glow permanente desde aquí
            boss.setRangedCooldown(20);
            boss.setBossState(BossState.IDLE);
        }
    }
}

package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.player.Player;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

/** Priority 0: dormido hasta que un jugador se acerca (el daño se gestiona en la entidad). */
public class BossSleepGoal extends AbstractBossGoal {
    private static final double WAKE_RADIUS = 10.0D;
    private int ticks;

    public BossSleepGoal(ForgottenConstructEntity boss) { super(boss); }

    @Override public boolean canUse() { return boss.getBossState() == BossState.SLEEP; }
    @Override public boolean canContinueToUse() { return boss.getBossState() == BossState.SLEEP; }

    @Override public void start() { ticks = 0; }

    @Override
    public void tick() {
        if (++ticks % 10 != 0) return;
        Player p = boss.level().getNearestPlayer(boss.getX(), boss.getY(), boss.getZ(), WAKE_RADIUS, true);
        if (p != null) boss.setBossState(BossState.WAKING_UP);
    }
}

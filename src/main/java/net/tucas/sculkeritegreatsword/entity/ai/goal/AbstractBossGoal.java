package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

import java.util.EnumSet;

/**
 * Base común. Todos los goals de "acción" comparten el flag MOVE (el jefe no se mueve, así que se usa
 * como candado): dos goals de acción nunca corren a la vez y el de menor número de prioridad interrumpe
 * al de mayor número.
 */
public abstract class AbstractBossGoal extends Goal {
    protected final ForgottenConstructEntity boss;

    protected AbstractBossGoal(ForgottenConstructEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    /** El GoalSelector solo actualiza los goals en ejecución cada 2 ticks salvo que se pida lo contrario. */
    @Override
    public boolean requiresUpdateEveryTick() { return true; }

    protected boolean hasValidTarget() {
        LivingEntity t = boss.getTarget();
        return t != null && t.isAlive()
                && (!(t instanceof Player p) || (!p.isCreative() && !p.isSpectator()));
    }

    /** Vuelve a IDLE salvo que otro sistema (aturdimiento, despertar) ya haya tomado el control del estado. */
    protected void returnToIdle() {
        BossState s = boss.getBossState();
        if (s != BossState.DAZED && s != BossState.WAKING_UP && s != BossState.SLEEP) {
            boss.setBossState(BossState.IDLE);
        }
    }
}

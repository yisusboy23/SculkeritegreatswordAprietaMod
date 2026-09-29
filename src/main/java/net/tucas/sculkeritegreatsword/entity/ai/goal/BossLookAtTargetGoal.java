package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

import java.util.EnumSet;

/** Priority 6: solo gira la cabeza (la base del cuerpo está bloqueada en la entidad). Corre en paralelo. */
public class BossLookAtTargetGoal extends Goal {
    private final ForgottenConstructEntity boss;

    public BossLookAtTargetGoal(ForgottenConstructEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = boss.getTarget();
        BossState s = boss.getBossState();
        return t != null && t.isAlive() && boss.isAwake() && s != BossState.DAZED && s != BossState.SLEEP;
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public void tick() {
        LivingEntity t = boss.getTarget();
        if (t != null) boss.getLookControl().setLookAt(t, 60.0F, 60.0F);
    }
}

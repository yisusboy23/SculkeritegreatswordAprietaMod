package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;

import java.util.EnumSet;

public class ResonarchPsychicAttackGoal extends Goal {

    private static final double RANGE = 20.0D;
    private static final float DAMAGE = 6.0F;

    private final ResonarchEntity boss;

    public ResonarchPsychicAttackGoal(ResonarchEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = boss.getTarget();
        return target != null
                && target.isAlive()
                && boss.isReadyForPsychicAttack()
                && boss.distanceToSqr(target) <= RANGE * RANGE
                && boss.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return false; // ataque instantáneo, no se mantiene el goal
    }

    @Override
    public void start() {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Daño mágico indirecto: ignora armadura, como golpe psíquico puro.
        target.hurt(boss.damageSources().indirectMagic(boss, boss), DAMAGE);

        // Empuje hacia atrás, alejando al jugador del boss
        Vec3 push = target.position().subtract(boss.position()).normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(push.x * 1.1D, 0.35D, push.z * 1.1D));
        target.hurtMarked = true;

        // Reutiliza la animación "reflect" como gesto visual del empujón psíquico,
        // aunque no haya ningún proyectil de por medio.
        boss.triggerAnim("bossController", "reflect");

        boss.onPsychicAttackUsed();
    }
}

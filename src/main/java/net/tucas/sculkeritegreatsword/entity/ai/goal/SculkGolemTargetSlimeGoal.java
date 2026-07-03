package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.tucas.sculkeritegreatsword.procedures.AttackCheckControlProcedure;

public class SculkGolemTargetSlimeGoal extends NearestAttackableTargetGoal<Slime> {

    private final Mob mob;

    public SculkGolemTargetSlimeGoal(Mob mob) {
        super(mob, Slime.class, true, true);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && AttackCheckControlProcedure.execute(mob);
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && AttackCheckControlProcedure.execute(mob);
    }
}

package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.tucas.sculkeritegreatsword.procedures.AttackCheckControlProcedure;

public class SculkGolemTargetPillagerGoal extends NearestAttackableTargetGoal<Pillager> {

    private final Mob mob;

    public SculkGolemTargetPillagerGoal(Mob mob) {
        super(mob, Pillager.class, true, true);
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

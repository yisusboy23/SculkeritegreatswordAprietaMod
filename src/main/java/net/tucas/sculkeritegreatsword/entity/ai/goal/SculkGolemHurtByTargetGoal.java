package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class SculkGolemHurtByTargetGoal extends HurtByTargetGoal {

    public SculkGolemHurtByTargetGoal(PathfinderMob mob) {
        super(mob);
    }

    @Override
    public boolean canUse() {
        return super.canUse(); // Aquí podrías agregar un procedimiento condicional si quieres
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }
}

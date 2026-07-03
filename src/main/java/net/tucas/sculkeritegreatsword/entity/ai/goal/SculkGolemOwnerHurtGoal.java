package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;

public class SculkGolemOwnerHurtGoal extends OwnerHurtByTargetGoal {

    public SculkGolemOwnerHurtGoal(TamableAnimal tamable) {
        super(tamable);
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }
}

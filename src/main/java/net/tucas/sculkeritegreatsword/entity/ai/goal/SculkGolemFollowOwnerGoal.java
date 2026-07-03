package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.Entity;

public class SculkGolemFollowOwnerGoal extends FollowOwnerGoal {
    private final TamableAnimal tamable;

    public SculkGolemFollowOwnerGoal(TamableAnimal tamable, double speedModifier,
                                     float startDistance, float stopDistance,
                                     boolean canFly) {
        super(tamable, speedModifier, startDistance, stopDistance, canFly);
        this.tamable = tamable;
    }

    @Override
    public boolean canUse() {
        Entity entity = this.tamable;
        return super.canUse() && entity.getPersistentData().getInt("golem_mode") == 1;
    }

    @Override
    public boolean canContinueToUse() {
        Entity entity = this.tamable;
        return super.canContinueToUse() && entity.getPersistentData().getInt("golem_mode") == 1;
    }
}
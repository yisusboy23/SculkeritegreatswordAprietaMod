package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.Entity;

public class SculkGolemWanderGoal extends WaterAvoidingRandomStrollGoal {
    private final TamableAnimal tamable;

    public SculkGolemWanderGoal(TamableAnimal tamable, double speedModifier) {
        super(tamable, speedModifier);
        this.tamable = tamable;
    }

    @Override
    public boolean canUse() {
        Entity entity = this.tamable;
        return super.canUse() && entity.getPersistentData().getInt("golem_mode") == 3;
    }
}
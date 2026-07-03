package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;

public class MudderRandomStrollGoal extends RandomStrollGoal {

    public MudderRandomStrollGoal(MudderEntity entity, double speedMultiplier) {
        super(entity, speedMultiplier);
    }

    @Override
    public boolean canUse() {
        return !this.mob.isInWaterOrBubble() && super.canUse();
    }
}

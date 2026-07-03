package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;

import javax.annotation.Nullable;

public class MudderRandomSwimGoal extends RandomStrollGoal {

    public MudderRandomSwimGoal(MudderEntity entity, double speedMultiplier, int interval) {
        super(entity, speedMultiplier, interval);
    }

    @Override
    public boolean canUse() {
        return this.mob.isInWaterOrBubble() && super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
    }
}

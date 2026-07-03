package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;

import java.util.EnumSet;

public class MudderEnterWaterGoal extends Goal {

    private final MudderEntity mudder;
    private final double speedModifier;
    private final int maxTimeOnLand;
    private BlockPos waterPos;
    private int timeOnLand = 0;

    public MudderEnterWaterGoal(MudderEntity mudder, double speedModifier, int maxTimeOnLand) {
        this.mudder = mudder;
        this.speedModifier = speedModifier;
        this.maxTimeOnLand = maxTimeOnLand;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mudder.isInWaterOrBubble()) return false;
        timeOnLand++;
        if (timeOnLand < maxTimeOnLand) return false;
        return findWaterPos();
    }

    @Override
    public boolean canContinueToUse() {
        return !mudder.getNavigation().isDone();
    }

    @Override
    public void start() {
        mudder.getNavigation().moveTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), speedModifier);
    }

    @Override
    public void stop() {
        timeOnLand = 0;
    }

    private boolean findWaterPos() {
        RandomSource random = mudder.getRandom();
        BlockPos.MutableBlockPos mPos = mudder.blockPosition().mutable();
        for (int i = 0; i < 10; i++) {
            mPos.move(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (mudder.level().getFluidState(mPos).is(FluidTags.WATER)) {
                waterPos = mPos.immutable();
                return true;
            }
        }
        return false;
    }
}

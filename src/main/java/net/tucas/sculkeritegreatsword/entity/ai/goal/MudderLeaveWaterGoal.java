package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;

import java.util.EnumSet;

public class MudderLeaveWaterGoal extends Goal {

    private final MudderEntity mudder;
    private final double speedModifier;
    private final int maxTimeInWater;
    private BlockPos landPos;
    private int timeInWater = 0;

    public MudderLeaveWaterGoal(MudderEntity mudder, double speedModifier, int maxTimeInWater) {
        this.mudder = mudder;
        this.speedModifier = speedModifier;
        this.maxTimeInWater = maxTimeInWater;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mudder.isInWaterOrBubble()) return false;
        timeInWater++;
        if (timeInWater < maxTimeInWater) return false;
        return findLandPos();
    }

    @Override
    public boolean canContinueToUse() {
        return mudder.isInWaterOrBubble();
    }

    @Override
    public void start() {
        mudder.getNavigation().moveTo(landPos.getX(), landPos.getY(), landPos.getZ(), speedModifier);
    }

    @Override
    public void stop() {
        timeInWater = 0;
    }

    @Override
    public void tick() {
        if (mudder.horizontalCollision && mudder.isInWaterOrBubble()) {
            float yRot = mudder.getYRot() * Mth.DEG_TO_RAD;
            mudder.setDeltaMovement(mudder.getDeltaMovement()
                    .add(-Mth.sin(yRot) * 0.3F, 0.26D, Mth.cos(yRot) * 0.3F));
        }
    }

    private boolean findLandPos() {
        RandomSource random = mudder.getRandom();
        Level level = mudder.level();
        BlockPos.MutableBlockPos mPos = mudder.blockPosition().mutable();
        for (int i = 0; i < 10; i++) {
            mPos.move(random.nextInt(20) - 10, 1 + random.nextInt(6), random.nextInt(20) - 10);
            if (level.getBlockState(mPos).isSolidRender(level, mPos)
                    && level.getBlockState(mPos.above()).isAir()
                    && mPos.getY() >= mudder.blockPosition().getY()) {
                this.landPos = mPos.immutable();
                return true;
            }
        }
        return false;
    }
}

package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.KrillathanEntity;

public class KrillathanLeapGoal extends JumpGoal {

    private static final int[] STEPS = {0, 1, 4, 5, 6, 7};
    private final KrillathanEntity krillathan;
    protected boolean breached;

    public KrillathanLeapGoal(KrillathanEntity entity) {
        this.krillathan = entity;
    }

    @Override
    public boolean canUse() {
        if (krillathan.isBabyEntity()) return false;
        if (krillathan.isVehicle()) return false;
        if (krillathan.getLeapState() != 0) return false;
        if (krillathan.getRandom().nextInt(reducedTickDelay(60)) != 0) return false;
        if (!krillathan.isInWaterOrBubble()) return false;

        Direction dir = krillathan.getMotionDirection();
        int dx = dir.getStepX();
        int dz = dir.getStepZ();
        BlockPos pos = krillathan.blockPosition();

        for (int k : STEPS) {
            BlockPos check = pos.offset(dx * k, 0, dz * k);
            if (!krillathan.level().getFluidState(check).is(FluidTags.WATER)) return false;
            if (!krillathan.level().getBlockState(pos.offset(dx * k, 1, dz * k)).isAir()) return false;
            if (!krillathan.level().getBlockState(pos.offset(dx * k, 2, dz * k)).isAir()) return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        double y = krillathan.getDeltaMovement().y;
        return (!(y * y < 0.03F) || krillathan.getXRot() == 0.0F ||
                !(Math.abs(krillathan.getXRot()) < 10.0F) || !krillathan.isInWater())
                && !krillathan.onGround()
                && krillathan.getLeapState() == 2
                && !krillathan.isInWater();
    }

    @Override
    public boolean isInterruptable() { return false; }

    @Override
    public void start() {
        Direction dir = krillathan.getMotionDirection();
        krillathan.setDeltaMovement(krillathan.getDeltaMovement().add(
                dir.getStepX() * 0.6,
                1.4,
                dir.getStepZ() * 0.6
        ));
        krillathan.getNavigation().stop();
        krillathan.setLeapState(2);
        breached = false;
    }

    @Override
    public void stop() {
        krillathan.setXRot(0.0F);
        krillathan.setLeapState(0);
    }

    @Override
    public void tick() {
        boolean flag = breached;
        if (!flag) {
            FluidState fluidstate = krillathan.level().getFluidState(krillathan.blockPosition());
            this.breached = fluidstate.is(FluidTags.WATER);
        }
        if (breached && !flag) {
            krillathan.playSound(SoundEvents.DOLPHIN_JUMP, 1.5F, 0.8F);
        }

        Vec3 vec3 = krillathan.getDeltaMovement();
        if (vec3.y * vec3.y < 0.03F && krillathan.getXRot() != 0.0F) {
            krillathan.setXRot(Mth.rotLerp(0.2F, krillathan.getXRot(), 0.0F));
        } else if (vec3.length() > 1.0E-5F) {
            double h = vec3.horizontalDistance();
            krillathan.setXRot((float)(Math.atan2(-vec3.y, h) * (180F / Math.PI)));
        }
    }

}
package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class MandrakeStrollGoal extends WaterAvoidingRandomStrollGoal {

    private final MandrakeEntity mandrake;

    public MandrakeStrollGoal(MandrakeEntity mandrake, double speed) {
        super(mandrake, speed);
        this.mandrake = mandrake;
    }

    @Override
    public boolean canUse() {
        return !mandrake.isAsleep() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !mandrake.isAsleep() && super.canContinueToUse();
    }
}
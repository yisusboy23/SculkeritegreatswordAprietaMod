package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenToyEntity;

import java.util.EnumSet;

public class ForgottenToySwellGoal extends Goal {
    private final ForgottenToyEntity toy;
    private LivingEntity target;

    public ForgottenToySwellGoal(ForgottenToyEntity toy) {
        this.toy = toy;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.toy.getTarget();
        return this.toy.getSwellDir() > 0 || (target != null && this.toy.distanceToSqr(target) < 9.0D);
    }

    @Override
    public void start() {
        this.toy.getNavigation().stop();
        this.target = this.toy.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            this.toy.setSwellDir(-1);
        } else if (this.toy.distanceToSqr(this.target) > 49.0D) {
            this.toy.setSwellDir(-1);
        } else if (!this.toy.getSensing().hasLineOfSight(this.target)) {
            this.toy.setSwellDir(-1);
        } else {
            this.toy.setSwellDir(1);
        }
    }
}
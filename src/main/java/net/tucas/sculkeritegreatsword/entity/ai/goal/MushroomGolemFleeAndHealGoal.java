package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;

import java.util.EnumSet;

/**
 * Goal simplificado - Solo HUYE, NO se cura automáticamente
 * La curación ahora es manual con Bone Meal
 */
public class MushroomGolemFleeAndHealGoal extends Goal {
    private final MushroomGolemEntity golem;
    private int fleeTicks = 0;
    private Vec3 fleePosition;

    public MushroomGolemFleeAndHealGoal(MushroomGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return golem.getHealth() / golem.getMaxHealth() < 0.25f &&
                golem.getTarget() != null;
    }

    @Override
    public void start() {
        fleeTicks = 0;
        golem.setRetreating(true);
        golem.setTaunting(false);
        findFleePosition();
    }

    @Override
    public void tick() {
        fleeTicks++;

        // Solo HUIR, sin curación automática
        if (fleePosition != null && fleeTicks <= 100) {
            golem.getNavigation().moveTo(fleePosition.x, fleePosition.y, fleePosition.z, 1.5);
        }

        // Dejar de huir después de 5 segundos
        if (fleeTicks >= 100) {
            stop();
        }
    }

    @Override
    public void stop() {
        fleeTicks = 0;
        golem.setRetreating(false);
        golem.setTaunting(true);
        golem.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return fleeTicks < 100;
    }

    private void findFleePosition() {
        LivingEntity target = golem.getTarget();
        if (target == null) {
            fleePosition = null;
            return;
        }

        Vec3 directionAway = golem.position().subtract(target.position()).normalize();
        fleePosition = golem.position().add(directionAway.scale(15.0));
    }
}
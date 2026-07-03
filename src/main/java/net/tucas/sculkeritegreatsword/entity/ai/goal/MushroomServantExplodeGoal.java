package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;

public class MushroomServantExplodeGoal extends Goal {
    private final MushroomServantEntity servant;
    private LivingEntity target;
    private int chargeTime = 0;
    private static final double ACTIVATION_DISTANCE = 9.0; // 3 bloques (3² = 9)

    public MushroomServantExplodeGoal(MushroomServantEntity servant) {
        this.servant = servant;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.servant.getTarget();
        // Activar cuando el enemigo esté a 3 bloques o menos
        return target != null && this.servant.distanceToSqr(target) < ACTIVATION_DISTANCE;
    }

    @Override
    public void start() {
        this.target = this.servant.getTarget();
        this.chargeTime = 0;
        // Comenzar con el estado 1 inmediatamente
        this.servant.setExplosionState(1);
    }

    @Override
    public void tick() {
        if (this.target == null || !this.target.isAlive()) {
            this.servant.setExplosionState(0);
            return;
        }

        chargeTime++;

        // Estado 1: Carga inicial (0-14 ticks)
        if (chargeTime >= 1 && chargeTime < 15) {
            this.servant.setExplosionState(1);
        }
        // Estado 2: Carga avanzada (15-29 ticks)
        else if (chargeTime >= 15 && chargeTime < 30) {
            this.servant.setExplosionState(2);
        }
        // Explotar a los 30 ticks (1.5 segundos)
        else if (chargeTime >= 30) {
            this.servant.explode();
            return;
        }

        // Moverse hacia el objetivo mientras se carga
        this.servant.getNavigation().moveTo(target, 1.5);
    }

    @Override
    public boolean canContinueToUse() {
        // Continuar hasta explotar, incluso si el enemigo se aleja
        return this.target != null && this.target.isAlive() && chargeTime < 30;
    }

    @Override
    public void stop() {
        this.servant.setExplosionState(0);
        this.chargeTime = 0;
        this.target = null;
    }
}
package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;

import java.util.EnumSet;

public class MushroomServantExplodeGoal extends Goal {
    private final MushroomServantEntity servant;
    private LivingEntity target;
    private int chargeTime = 0;

    private static final double CHARGE_DISTANCE_SQR = 9.0;

    // NUEVOS: Constantes para tiempos de carga más rápidos
    private static final int CHARGE_START = 1;
    private static final int CHARGE_MID = 8;    // Antes era 15
    private static final int CHARGE_MAX = 15;    // Antes era 30

    public MushroomServantExplodeGoal(MushroomServantEntity servant) {
        this.servant = servant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.servant.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        this.target = this.servant.getTarget();
        this.chargeTime = 0;
        this.servant.setExplosionState(0);
    }

    @Override
    public void tick() {
        if (this.target == null || !this.target.isAlive()) {
            this.servant.setExplosionState(0);
            return;
        }

        double distSqr = this.servant.distanceToSqr(this.target);

        if (distSqr > CHARGE_DISTANCE_SQR) {
            this.servant.getNavigation().moveTo(this.target, 1.5);
            this.servant.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            chargeTime = 0;
            this.servant.setExplosionState(0);
            return;
        }

        chargeTime++;

        // Estados de carga más rápidos
        if (chargeTime >= CHARGE_START && chargeTime < CHARGE_MID) {
            this.servant.setExplosionState(1);
        } else if (chargeTime >= CHARGE_MID && chargeTime < CHARGE_MAX) {
            this.servant.setExplosionState(2);
        } else if (chargeTime >= CHARGE_MAX) {
            this.servant.explode();
            return;
        }

        this.servant.getNavigation().moveTo(this.target, 1.5);
        this.servant.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && chargeTime < CHARGE_MAX;
    }

    @Override
    public void stop() {
        this.servant.setExplosionState(0);
        this.chargeTime = 0;
        this.target = null;
    }
}
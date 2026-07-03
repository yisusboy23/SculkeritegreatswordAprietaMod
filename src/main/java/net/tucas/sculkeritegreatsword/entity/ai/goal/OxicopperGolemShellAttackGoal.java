package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;

import java.util.EnumSet;

public class OxicopperGolemShellAttackGoal extends Goal {

    private final OxicopperGolemEntity golem;
    private final double attackRange;
    private final double safeDistance;
    private LivingEntity target;
    private int timeSinceLastShot = 0;

    public OxicopperGolemShellAttackGoal(OxicopperGolemEntity golem, double attackRange, double safeDistance) {
        this.golem = golem;
        this.attackRange = attackRange;
        this.safeDistance = safeDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = this.golem.getTarget();

        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        double distSqr = this.golem.distanceToSqr(this.target);

        // CAMBIO: Activar si está dentro del rango de ataque (sin mínimo de distancia segura)
        // Esto permite que ataque incluso si el enemigo está cerca
        return distSqr <= (this.attackRange * this.attackRange);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        if (!this.golem.shouldStayInShell()) {
            return false;
        }

        double distSqr = this.golem.distanceToSqr(this.target);

        // Salir si está DEMASIADO lejos
        if (distSqr > (this.attackRange * 1.5 * this.attackRange * 1.5)) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.golem.enterShell();
        this.timeSinceLastShot = 0;
        this.golem.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.golem.exitShell();
        this.target = null;
        this.timeSinceLastShot = 0;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        this.golem.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        this.timeSinceLastShot++;

        if (this.timeSinceLastShot >= 25 && this.golem.canShoot()) {
            this.golem.startShootSequence(this.target);
            this.timeSinceLastShot = 0;
        }
    }
}
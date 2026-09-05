package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;

import java.util.EnumSet;

public class ResonarchLookAtPlayerGoal extends Goal {

    private static final double LOOK_DISTANCE = 20.0D;

    private final ResonarchEntity resonarch;

    private Player target;

    public ResonarchLookAtPlayerGoal(ResonarchEntity resonarch) {
        this.resonarch = resonarch;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.resonarch.isExclusiveActionActive()) {
            return false;
        }

        Player player = this.resonarch.level().getNearestPlayer(
                this.resonarch,
                LOOK_DISTANCE
        );

        if (player == null || !player.isAlive()) {
            return false;
        }

        this.target = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.resonarch.isExclusiveActionActive()) {
            return false;
        }

        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        return this.resonarch.distanceToSqr(this.target)
                <= LOOK_DISTANCE * LOOK_DISTANCE;
    }

    @Override
    public void start() {
        this.lookAtPlayer();
    }

    @Override
    public void tick() {
        this.lookAtPlayer();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    private void lookAtPlayer() {
        if (this.target == null) {
            return;
        }

        // Mira con la cabeza hacia el jugador.
        this.resonarch.getLookControl().setLookAt(
                this.target,
                30.0F,
                30.0F
        );

        // Hace que el cuerpo también se oriente hacia el jugador.
        double dx = this.target.getX() - this.resonarch.getX();
        double dz = this.target.getZ() - this.resonarch.getZ();

        float yaw = (float) (
                Math.atan2(dz, dx) * 180.0D / Math.PI
        ) - 90.0F;

        this.resonarch.yBodyRot = yaw;
        this.resonarch.yBodyRotO = yaw;
    }
}
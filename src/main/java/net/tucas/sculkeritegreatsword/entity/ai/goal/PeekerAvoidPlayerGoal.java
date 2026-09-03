package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.tucas.sculkeritegreatsword.entity.custom.PeekerEntity;

/**
 * Igual que AvoidEntityGoal, pero deja de aplicar en cuanto el Peeker es domesticado.
 * Así evitamos tener que registrar dos juegos de Goals distintos (uno para "salvaje"
 * y otro para "domesticado") o crear una segunda clase de entidad solo por esto.
 */
public class PeekerAvoidPlayerGoal extends AvoidEntityGoal<Player> {

    private final PeekerEntity peeker;

    public PeekerAvoidPlayerGoal(PeekerEntity peeker, float maxDistance, double walkSpeed, double sprintSpeed) {
        super(peeker, Player.class, maxDistance, walkSpeed, sprintSpeed);
        this.peeker = peeker;
    }

    @Override
    public boolean canUse() {
        return !peeker.isTame() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !peeker.isTame() && super.canContinueToUse();
    }
}
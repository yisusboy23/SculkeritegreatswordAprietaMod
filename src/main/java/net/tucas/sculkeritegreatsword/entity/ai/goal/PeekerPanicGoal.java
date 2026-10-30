package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.tucas.sculkeritegreatsword.entity.custom.PeekerEntity;

/** Entra en pánico al ser golpeado, salvo si el atacante es su dueño. */
public class PeekerPanicGoal extends PanicGoal {

    private final PeekerEntity peeker;

    public PeekerPanicGoal(PeekerEntity peeker, double speed) {
        super(peeker, speed);
        this.peeker = peeker;
    }

    @Override
    protected boolean shouldPanic() {
        LivingEntity attacker = peeker.getLastHurtByMob();
        if (peeker.isTame() && attacker != null && peeker.isOwnedBy(attacker)) {
            return false;
        }
        return super.shouldPanic();
    }
}
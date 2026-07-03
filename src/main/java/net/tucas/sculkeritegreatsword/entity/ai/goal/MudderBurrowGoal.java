


package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;

import java.util.EnumSet;

public class MudderBurrowGoal extends Goal {

    private final MudderEntity mudder;
    private int burrowTimer   = 0;
    private int burrowedTimer = 0;
    private boolean shouldStop = false;  // ← flag para salida limpia

    public MudderBurrowGoal(MudderEntity mudder) {
        this.mudder = mudder;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (mudder.isInWaterOrBubble()) return false;
        if (mudder.isBaby()) return false;
        if (mudder.burrowCooldown > 0) return false;
        if (mudder.getRandom().nextInt(400) != 0) return false;
        return canBurrowHere();
    }
    @Override
    public boolean canContinueToUse() {
        if (shouldStop) return false;
        if (mudder.isInWaterOrBubble()) return false;
        if (!canBurrowHere()) return false;
        if (!mudder.isBurrowed() && !mudder.isBurrowing()) return false; // ← sale si stopBurrow() fue llamado externamente
        return true;
    }

    @Override
    public void start() {
        burrowTimer   = 0;
        burrowedTimer = 200 + mudder.getRandom().nextInt(400);
        shouldStop    = false;
        mudder.getNavigation().stop();
        mudder.startBurrow();
    }

    @Override
    public void tick() {
        mudder.getNavigation().stop();
        burrowTimer++;

        if (mudder.isBurrowing() && burrowTimer >= 20) {
            mudder.finishBurrow();
        }

        if (mudder.isBurrowed()) {
            burrowedTimer--;
            if (burrowedTimer <= 0) {
                shouldStop = true;  // ← NO llamar stop() directamente
            }
        }
    }
    @Override
    public void stop() {
        burrowTimer  = 0;
        shouldStop   = false;
        mudder.stopBurrow();
    }

    private boolean canBurrowHere() {
        var below   = mudder.level().getBlockState(mudder.blockPosition().below());
        var current = mudder.level().getBlockState(mudder.blockPosition());
        return below.is(Blocks.MUD) || current.is(Blocks.MUD);
    }
}
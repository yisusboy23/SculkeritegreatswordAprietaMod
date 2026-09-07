package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.tucas.sculkeritegreatsword.entity.custom.WoolodonEntity;

public class WoolodonEatGrassGoal extends EatBlockGoal {

    private static final int EAT_COOLDOWN = 100;

    private final WoolodonEntity woolodon;

    /*
     * Contador propio para saber cuándo terminó
     * la acción de comer.
     */
    private int woolodonEatTicks = 0;

    /*
     * Duración aproximada de la acción de comer.
     */
    private static final int EAT_DURATION = 20;

    public WoolodonEatGrassGoal(
            WoolodonEntity woolodon
    ) {
        super(woolodon);
        this.woolodon = woolodon;
    }

    @Override
    public void start() {

        super.start();

        woolodonEatTicks = 0;

        /*
         * Tanto adultos como bebés reproducen
         * la animación de comer.
         */
        if (woolodon.getEatCooldown() <= 0) {

            woolodon.playEatAnim();

            woolodon.setEatCooldown(
                    EAT_COOLDOWN
            );
        }
    }

    @Override
    public void tick() {

        super.tick();

        woolodonEatTicks++;

        /*
         * Cuando termina el ciclo de comer,
         * regeneramos la lana.
         *
         * regenerateWool() ya comprueba si es bebé,
         * por lo que los bebés no recuperarán lana.
         */
        if (woolodonEatTicks == EAT_DURATION) {

            woolodon.regenerateWool();
        }
    }

    @Override
    public void stop() {

        super.stop();

        woolodonEatTicks = 0;
    }
}
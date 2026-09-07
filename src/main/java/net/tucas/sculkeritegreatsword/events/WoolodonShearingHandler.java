package net.tucas.sculkeritegreatsword.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.WoolodonEntity;
import net.minecraft.sounds.SoundEvents;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID)
public class WoolodonShearingHandler {

    /*
     * Las 16 lanas vanilla de Minecraft.
     *
     * El color se seleccionará aleatoriamente.
     */
    private static final Item[] WOOL_ITEMS = {
            Items.WHITE_WOOL,
            Items.ORANGE_WOOL,
            Items.MAGENTA_WOOL,
            Items.LIGHT_BLUE_WOOL,
            Items.YELLOW_WOOL,
            Items.LIME_WOOL,
            Items.PINK_WOOL,
            Items.GRAY_WOOL,
            Items.LIGHT_GRAY_WOOL,
            Items.CYAN_WOOL,
            Items.PURPLE_WOOL,
            Items.BLUE_WOOL,
            Items.BROWN_WOOL,
            Items.GREEN_WOOL,
            Items.RED_WOOL,
            Items.BLACK_WOOL
    };

    @SubscribeEvent
    public static void onWoolodonInteract(
            PlayerInteractEvent.EntityInteract event
    ) {

        // =====================================================
        // SOLO MANO PRINCIPAL
        // =====================================================

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }


        // =====================================================
        // COMPROBAR WOOLODON
        // =====================================================

        if (!(event.getTarget() instanceof WoolodonEntity woolodon)) {
            return;
        }


        // =====================================================
        // COMPROBAR JUGADOR
        // =====================================================

        Player player = event.getEntity();


        // =====================================================
        // COMPROBAR TIJERAS
        // =====================================================

        ItemStack stack =
                player.getItemInHand(event.getHand());

        if (!stack.is(Items.SHEARS)) {
            return;
        }


        // =====================================================
        // LOS BEBÉS NO PUEDEN SER ESQUILADOS
        // =====================================================

        if (woolodon.isBaby()) {
            return;
        }


        // =====================================================
        // CORTAR UNA SOLA PARTE
        // =====================================================

        int cutPart =
                woolodon.shearRandomWoolPart();

        /*
         * -1 significa que ya no queda ninguna
         * parte de lana disponible.
         */
        if (cutPart == -1) {
            return;
        }


        // =====================================================
        // ELEGIR LANA VANILLA ALEATORIA
        // =====================================================

        Item randomWool =
                WOOL_ITEMS[
                        woolodon.getRandom().nextInt(
                                WOOL_ITEMS.length
                        )
                        ];


        // =====================================================
        // CREAR LA LANA
        // =====================================================

        ItemStack woolStack = new ItemStack(
                randomWool,
                2 + woolodon.getRandom().nextInt(5)
        );
        woolodon.spawnAtLocation(woolStack);
        woolodon.playSound(
                SoundEvents.SHEEP_SHEAR,
                1.0F,
                1.0F
        );

        // =====================================================
        // DESGASTAR TIJERAS
        // =====================================================

        stack.hurtAndBreak(
                1,
                player,
                p -> p.broadcastBreakEvent(
                        event.getHand()
                )
        );


        // =====================================================
        // CANCELAR INTERACCIÓN NORMAL
        // =====================================================

        event.setCancellationResult(
                InteractionResult.sidedSuccess(
                        woolodon.level().isClientSide
                )
        );

        event.setCanceled(true);
    }
}
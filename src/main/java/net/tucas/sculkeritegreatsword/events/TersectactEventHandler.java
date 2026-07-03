package net.tucas.sculkeritegreatsword.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.item.TersectactItem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.WaterAnimal;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TersectactEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

        Player player = event.getEntity();
        Entity targetEntity = event.getTarget();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (player.level().isClientSide) return;
        if (!(stack.getItem() instanceof TersectactItem)) return;
        if (!(targetEntity instanceof LivingEntity target)) return;
        if (!(target instanceof Animal) && !(target instanceof WaterAnimal) && !(target instanceof TamableAnimal)) {
            return;
        }
        if (target instanceof Player) return;

        int storedCount = TersectactItem.getStoredCount(stack);

        if (storedCount >= 3) {
            player.displayClientMessage(
                    Component.literal("El Tersectact está lleno (3/3)").withStyle(ChatFormatting.RED),
                    true
            );
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            return;
        }

        if (TersectactItem.storeEntity(stack, target, player)) {

            target.remove(Entity.RemovalReason.DISCARDED);

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    1.0F, 1.2F
            );

            player.displayClientMessage(
                    Component.literal("Criatura capturada (" + (storedCount + 1) + "/3)")
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            ItemStack newStack = stack.copy();
            player.setItemInHand(hand, newStack);
            player.stopUsingItem();

            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
        }
    }
}
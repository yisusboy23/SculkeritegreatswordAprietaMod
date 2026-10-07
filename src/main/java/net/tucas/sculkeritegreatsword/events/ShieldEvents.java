package net.tucas.sculkeritegreatsword.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

@Mod.EventBusSubscriber(modid = "sculkeritegreatsword")
public class ShieldEvents {

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent e) {
        if (e.getDamageSource().getDirectEntity() instanceof LargeFireball fb
                && fb.getPersistentData().getBoolean(ForgottenConstructEntity.FIREBALL_TAG)
                && e.getEntity() instanceof Player p) {
            ItemStack s = p.getUseItem();
            int left = s.getMaxDamage() - s.getDamageValue() - 3;   // deja ~3 de durabilidad
            if (left > 0) s.hurtAndBreak(left, p, pl -> pl.broadcastBreakEvent(p.getUsedItemHand()));
            e.setShieldTakesDamage(false);                          // evita que el daño vanilla lo termine de romper
        }
    }
}
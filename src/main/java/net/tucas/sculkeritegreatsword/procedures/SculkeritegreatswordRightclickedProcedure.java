package net.tucas.sculkeritegreatsword.procedures;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import net.tucas.sculkeritegreatsword.init.ModEnchantments;
import net.tucas.sculkeritegreatsword.item.Moditems;
import net.tucas.sculkeritegreatsword.init.ModMobEffects;
import net.tucas.sculkeritegreatsword.init.ModSounds;

public class SculkeritegreatswordRightclickedProcedure {

    public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (entity == null) return;

        // Enfriamiento según encantamiento
        if (entity instanceof Player player) {
            int cooldown = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FASTRECHARGE.get(), itemstack) > 0 ? 90 : 180;
            player.getCooldowns().addCooldown(itemstack.getItem(), cooldown);
        }

        // Aplicar efecto según encantamiento
        if (entity instanceof LivingEntity living) {
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.KINETICENERGY.get(), itemstack) > 0) {
                living.addEffect(new MobEffectInstance(ModMobEffects.SCULKERITEGREATSWORDKINETICENERGYDASH.get(), 30, 0, false, false));
            } else {
                living.addEffect(new MobEffectInstance(ModMobEffects.SCULKERITEGREATSWORD_DASHING.get(), 30, 0, false, false));
            }
        }

        // Reproducir sonido
        if (world instanceof Level level) {
            level.playSound(null,
                    BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                    ModSounds.SONIC_BOOM.get(),
                    SoundSource.PLAYERS,
                    0.5f,
                    1.0f);

        }

        // Posible daño al ítem (si no está en modo creativo)
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            if (itemstack.hurt(1, RandomSource.create(), null)) {
                itemstack.shrink(1);
                itemstack.setDamageValue(0);
            }
        }

        // Animación de mano
        if (entity instanceof LivingEntity living) {
            if (living.getOffhandItem().getItem() == Moditems.SCULKERITEGREATSWORD.get()) {
                living.swing(InteractionHand.OFF_HAND, true);
            } else if (living.getMainHandItem().getItem() == Moditems.SCULKERITEGREATSWORD.get()) {
                living.swing(InteractionHand.MAIN_HAND, true);
            }
        }
    }
}
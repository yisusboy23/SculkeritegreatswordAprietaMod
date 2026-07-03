package net.tucas.sculkeritegreatsword.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.MobSpawnType;
import net.tucas.sculkeritegreatsword.init.ModEnchantments;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.init.ModSounds;

import java.util.List;

public class SculkeriteScytheRightclickedProcedure {

    public static void execute(Level level, Player player, ItemStack itemstack) {
        if (level.isClientSide() || player == null) return;

        // Verificar encantamiento KINETICENERGY
        int kineticLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.KINETICENERGY.get(), itemstack);
        if (kineticLevel <= 0) return;

        // Aplicar cooldown
        int cooldown = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FASTRECHARGE.get(), itemstack) > 0 ? 90 : 180;
        player.getCooldowns().addCooldown(itemstack.getItem(), cooldown);

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // Sonido de explosión sónica
        level.playSound(null, BlockPos.containing(x, y, z), ModSounds.SONIC_BOOM.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        // Entidad decorativa
        if (level instanceof ServerLevel serverLevel) {
            EntityType<?> visualEntity = ModEntities.SCULKBOOMPARTICLE.get();
            double radius = 5.0;
            for (int angle = 0; angle < 360; angle += 45) {
                double rad = Math.toRadians(angle);
                double px = x + Math.cos(rad) * radius;
                double pz = z + Math.sin(rad) * radius;
                double py = y + 1.2;

                BlockPos spawnPos = BlockPos.containing(px, py, pz);
                if (serverLevel.getBlockState(spawnPos).isAir() || serverLevel.getBlockState(spawnPos).canBeReplaced()) {
                    visualEntity.spawn(serverLevel, spawnPos, MobSpawnType.MOB_SUMMONED);
                }
            }

            DamageSource silentDamage = new DamageSource(
                    serverLevel.registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.MAGIC)
            );

            // Área de daño
            AABB area = new AABB(x - 7, y - 5, z - 7, x + 7, y + 5, z + 7);

            // FILTRAR MASCOTAS DEL JUGADOR
            List<Entity> targets = level.getEntities(player, area, e -> {
                if (!(e instanceof LivingEntity)) return false;
                if (e == player) return false;

                // NO dañar mascotas del jugador
                if (e instanceof TamableAnimal tamable) {
                    return !tamable.isOwnedBy(player);
                }

                return true;
            });

            targets.forEach(target -> {
                net.tucas.sculkeritegreatsword.Sculkeritegreatsword.queueServerWork(1, () -> {
                    // Doble verificación antes de hacer daño
                    if (target instanceof TamableAnimal tamable && tamable.isOwnedBy(player)) {
                        return;
                    }
                    target.hurt(silentDamage, 28.0f);
                });
            });
        }

        // Animación de brazo
        InteractionHand handUsed = player.getMainHandItem() == itemstack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        player.swing(handUsed, true);
    }
}
package net.tucas.sculkeritegreatsword.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.init.ModParticles;
import net.tucas.sculkeritegreatsword.init.ModSounds;

import java.util.List;

public class SculkeritegreatswordLivingEntityIsHitWithToolProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceEntity) {
        if (entity == null || sourceEntity == null) return;

        // Reproducir sonido al impactar
        if (world instanceof Level level && !level.isClientSide()) {
            SoundEvent hitSound = ModSounds.SWORD_HIT.get();
            if (hitSound != null) {
                level.playSound(null, BlockPos.containing(x, y, z), hitSound, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // Mostrar partícula sweep
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    (SimpleParticleType) ModParticles.SCULKERITEGOLEMSWEEP.get(),
                    entity.getX(), entity.getY() + 0.85, entity.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0
            );

            // Buscar entidades cercanas
            Vec3 center = new Vec3(x, y, z);
            AABB area = new AABB(center, center).inflate(2.0);

            List<Entity> nearbyEntities = serverLevel.getEntitiesOfClass(Entity.class, area).stream()
                    .filter(e -> e != entity && e != sourceEntity)
                    .filter(e -> {
                        // Proteger mascotas del jugador
                        if (e instanceof TamableAnimal tamable && sourceEntity instanceof Player player) {
                            return !tamable.isOwnedBy(player);
                        }
                        return true;
                    })
                    .toList();

            DamageSource source = new DamageSource(
                    serverLevel.registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.MAGIC)
            );

            for (Entity target : nearbyEntities) {
                if (target instanceof LivingEntity living) {
                    // Doble verificación
                    if (target instanceof TamableAnimal tamable && sourceEntity instanceof Player player) {
                        if (tamable.isOwnedBy(player)) {
                            continue;
                        }
                    }
                    living.hurt(source, 3.0f);
                }
            }
        }
    }
}
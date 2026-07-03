package net.tucas.sculkeritegreatsword.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.init.ModParticles;
import net.tucas.sculkeritegreatsword.init.ModSounds;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;

public class SculkeritegreatswordkineticenergydashEffectExpiresProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) return;

        if (world instanceof ServerLevel serverLevel) {
            // Partículas
            serverLevel.sendParticles((SimpleParticleType) ModParticles.SCULKEXPLOSION.get(), x, y, z, 32, 1.5, 1.5, 1.5, 0.0);

            // Sonido
            SoundEvent explosionSound = ModSounds.EXPLOSION.get();
            if (explosionSound != null) {
                serverLevel.playSound(null, BlockPos.containing(x, y, z), explosionSound, SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            // Entidad decorativa
            EntityType<?> entityType = ModEntities.SCULKBOOMPARTICLE.get();
            if (entityType != null) {
                entityType.spawn(serverLevel, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
            }

            Vec3 center = new Vec3(x, y, z);
            AABB area = new AABB(center, center).inflate(7.5);

            List<Entity> targets = serverLevel.getEntitiesOfClass(Entity.class, area).stream()
                    .filter(e -> e instanceof LivingEntity)
                    .filter(e -> e != entity)
                    .filter(e -> {
                        // Proteger mascotas del jugador
                        if (e instanceof TamableAnimal tamable && entity instanceof Player player) {
                            return !tamable.isOwnedBy(player);
                        }
                        return true;
                    })
                    .toList();

            for (Entity target : targets) {
                float damage = Mth.nextInt(RandomSource.create(), 14, 28);

                Sculkeritegreatsword.queueServerWork(1, () -> {
                    // Doble verificación
                    if (target instanceof TamableAnimal tamable && entity instanceof Player player) {
                        if (tamable.isOwnedBy(player)) {
                            return;
                        }
                    }

                    DamageSource source = new DamageSource(
                            serverLevel.registryAccess()
                                    .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(DamageTypes.MAGIC)
                    );
                    target.hurt(source, damage);
                });
            }
        }
    }
}
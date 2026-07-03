package net.tucas.sculkeritegreatsword.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.init.ModSounds;
import net.tucas.sculkeritegreatsword.events.GolemExplosionTracker;

import java.util.List;

public class SculkGolemDeathExplosionProcedure {

    public static void execute(Level level, Entity sourceEntity, double x, double y, double z) {
        if (level.isClientSide()) return;

        // Sonido de explosión
        level.playSound(null, BlockPos.containing(x, y, z), ModSounds.SONIC_BOOM.get(), SoundSource.HOSTILE, 1.0f, 1.0f);

        if (level instanceof ServerLevel serverLevel) {
            // Entidad visual
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

            // Daño mágico silencioso
            DamageSource silentDamage = new DamageSource(
                    serverLevel.registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.MAGIC)
            );

            AABB area = new AABB(x - 7, y - 5, z - 7, x + 7, y + 5, z + 7);
            List<Entity> targets = level.getEntities(sourceEntity, area, e -> e instanceof LivingEntity && e != sourceEntity);

            targets.forEach(target -> {
                GolemExplosionTracker.markedForDeathByGolem.add(target.getUUID()); // ⚠️ Marca para disco
                Sculkeritegreatsword.queueServerWork(1, () -> {
                    target.hurt(silentDamage, 28.0f);
                });
            });
        }
    }
}

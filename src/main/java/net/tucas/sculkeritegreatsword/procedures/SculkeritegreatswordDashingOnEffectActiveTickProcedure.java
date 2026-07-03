package net.tucas.sculkeritegreatsword.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModSounds;

import java.util.Comparator;
import java.util.List;

public class SculkeritegreatswordDashingOnEffectActiveTickProcedure {

    public static final TagKey<Block> FORGE_GLASS =
            TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "glass"));

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) return;

        // Aplicar efectos de velocidad y salto
        if (entity instanceof LivingEntity living && !living.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 6, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP, 1, 2, false, false));
        }

        // Aplicar impulso de dash
        Vec3 direction = entity.getLookAngle().normalize().scale(1.0);
        entity.setDeltaMovement(direction);
        entity.hurtMarked = true;

        // Partículas
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, x, y + 1.0, z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM, x, y + 1.0, z, 1, 0, 0, 0, 0);
        }

        // Daño a entidades cercanas (PROTEGIENDO MASCOTAS)
        if (world instanceof ServerLevel serverLevel) {
            Vec3 center = new Vec3(x, y, z);
            List<Entity> entities = serverLevel.getEntities(entity, new AABB(center, center).inflate(2.0))
                    .stream()
                    .filter(e -> e != entity)
                    .filter(e -> e instanceof LivingEntity)
                    .filter(e -> {
                        // Proteger mascotas del jugador que hace el dash
                        if (e instanceof TamableAnimal tamable && entity instanceof Player player) {
                            return !tamable.isOwnedBy(player);
                        }
                        return true;
                    })
                    .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                    .toList();

            for (Entity nearby : entities) {
                DamageSource source = serverLevel.damageSources().sonicBoom(entity);
                Sculkeritegreatsword.queueServerWork(1, () -> {
                    // Doble verificación antes de hacer daño
                    if (nearby instanceof TamableAnimal tamable && entity instanceof Player player) {
                        if (tamable.isOwnedBy(player)) {
                            return; // NO dañar mascotas propias
                        }
                    }
                    nearby.hurt(source, 4.0f);
                });
            }
        }

        // Romper bloques frágiles
        int radius = 2;
        for (int yi = -radius; yi <= radius; yi++) {
            for (int xi = -radius; xi <= radius; xi++) {
                for (int zi = -radius; zi <= radius; zi++) {
                    double distanceSq = (xi * xi) / 4.0 + (yi * yi) / 4.0 + (zi * zi) / 4.0;
                    if (distanceSq <= 1.0) {
                        BlockPos pos = BlockPos.containing(x + xi, y + yi, z + zi);
                        if (world.getBlockState(pos).is(BlockTags.LEAVES)
                                || world.getBlockState(pos).is(FORGE_GLASS)
                                || world.getBlockState(pos).is(Blocks.SNOW)) {
                            world.destroyBlock(pos, false);
                        }
                    }
                }
            }
        }

        // Detectar colisión con bloque sólido frente al jugador y reproducir sonido una vez
        Vec3 lookVec = entity.getLookAngle();
        BlockPos frente = BlockPos.containing(x + lookVec.x, y + lookVec.y, z + lookVec.z);
        boolean colision = world.getBlockState(frente).isSolid();

        if (colision && entity.getPersistentData().getBoolean("stillDashing")) {
            entity.getPersistentData().putBoolean("stillDashing", false);

            if (world instanceof Level level && !level.isClientSide()) {
                SoundEvent dashHit = ModSounds.DASH_HIT.get();
                if (dashHit != null) {
                    level.playSound(null, BlockPos.containing(x, y, z), dashHit, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
        }

        // Mientras sigue dashing, mantener el estado
        if (!colision) {
            entity.getPersistentData().putBoolean("stillDashing", true);
        }
    }
}
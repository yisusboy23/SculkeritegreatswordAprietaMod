package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MushroomGolemAuraGoal extends Goal {
    private final MushroomGolemEntity golem;
    private int tickCounter = 0;
    private static final double AURA_RADIUS = 3.0;
    private static final double ENEMY_RADIUS = 3.5;

    // Sistema de cooldown para efectos instantáneos
    private final Map<UUID, Long> lastInstantEffectTime = new HashMap<>();
    private static final int INSTANT_EFFECT_COOLDOWN = 100; // 5 segundos (100 ticks)

    public MushroomGolemAuraGoal(MushroomGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return golem.hasActivePotion() && golem.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        tickCounter++;

        if (tickCounter >= 20) {
            tickCounter = 0;
            MobEffectInstance effect = golem.getStoredEffect();
            if (effect != null) {
                boolean isBeneficial = effect.getEffect().isBeneficial();
                if (isBeneficial) {
                    applyToAllies(effect);
                } else {
                    applyToEnemies(effect);
                }
            }
        }

        if (golem.tickCount % 5 == 0) {
            spawnAuraParticles();
        }

        // Limpiar cooldowns antiguos cada 10 segundos para evitar memory leaks
        if (golem.tickCount % 200 == 0) {
            cleanupOldCooldowns();
        }
    }

    private void applyToAllies(MobEffectInstance effect) {
        AABB area = new AABB(golem.blockPosition()).inflate(AURA_RADIUS);
        List<LivingEntity> entities = golem.level().getEntitiesOfClass(
                LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            // GOLEM NUNCA RECIBE EFECTOS
            if (entity != golem && isAlly(entity)) {
                // Verificar cooldown si es efecto instantáneo
                if (effect.getEffect().isInstantenous()) {
                    if (!canApplyInstantEffect(entity)) {
                        continue; // Saltar esta entidad, está en cooldown
                    }
                }

                entity.addEffect(new MobEffectInstance(
                        effect.getEffect(),
                        40,
                        effect.getAmplifier(),
                        true,
                        true
                ));

                // Registrar el tiempo si fue instantáneo
                if (effect.getEffect().isInstantenous()) {
                    lastInstantEffectTime.put(entity.getUUID(), golem.level().getGameTime());
                }
            }
        }
    }

    private void applyToEnemies(MobEffectInstance effect) {
        AABB area = new AABB(golem.blockPosition()).inflate(ENEMY_RADIUS);
        List<LivingEntity> enemies = golem.level().getEntitiesOfClass(
                LivingEntity.class, area,
                entity -> entity instanceof Enemy && entity.isAlive());

        for (LivingEntity enemy : enemies) {
            // Verificar cooldown si es efecto instantáneo
            if (effect.getEffect().isInstantenous()) {
                if (!canApplyInstantEffect(enemy)) {
                    continue; // Saltar este enemigo, está en cooldown
                }
            }

            enemy.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    40,
                    effect.getAmplifier(),
                    true,
                    true
            ));

            // Registrar el tiempo si fue instantáneo
            if (effect.getEffect().isInstantenous()) {
                lastInstantEffectTime.put(enemy.getUUID(), golem.level().getGameTime());
            }
        }
    }

    /**
     * Verifica si puede aplicar un efecto instantáneo a una entidad
     * basándose en el cooldown
     */
    private boolean canApplyInstantEffect(LivingEntity entity) {
        long currentTime = golem.level().getGameTime();
        long lastTime = lastInstantEffectTime.getOrDefault(entity.getUUID(), 0L);

        return (currentTime - lastTime) >= INSTANT_EFFECT_COOLDOWN;
    }

    /**
     * Limpia cooldowns de entidades que ya no existen o están muy viejos
     * para evitar memory leaks
     */
    private void cleanupOldCooldowns() {
        long currentTime = golem.level().getGameTime();
        lastInstantEffectTime.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > 1200 // 1 minuto
        );
    }

    private boolean isAlly(LivingEntity entity) {
        return (golem.getOwner() != null && entity == golem.getOwner());
    }

    private void spawnAuraParticles() {
        if (golem.level() instanceof ServerLevel serverLevel) {
            MobEffectInstance effect = golem.getStoredEffect();
            boolean isBeneficial = (effect != null && effect.getEffect().isBeneficial());

            for (int i = 0; i < 1; i++) {
                double offsetX = (golem.getRandom().nextDouble() - 0.5) * 1.2;
                double offsetY = golem.getRandom().nextDouble() * 1.0;
                double offsetZ = (golem.getRandom().nextDouble() - 0.5) * 1.2;

                if (isBeneficial) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            golem.getX() + offsetX,
                            golem.getY() + offsetY,
                            golem.getZ() + offsetZ,
                            1,
                            0.05, 0.05, 0.05,
                            0.0);
                } else {
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            golem.getX() + offsetX,
                            golem.getY() + offsetY,
                            golem.getZ() + offsetZ,
                            1,
                            0.05, 0.05, 0.05,
                            0.0);
                }
            }
        }
    }
}
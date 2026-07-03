package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MushroomGolemGasAttackGoal extends Goal {
    private final MushroomGolemEntity golem;
    private int gasDuration = 0;
    private final Map<UUID, Integer> enemyExposureTime = new HashMap<>();
    private static final int GAS_TOTAL_DURATION = 240;
    private static final double GAS_RADIUS = 3.5;
    private static final double ENEMY_DETECTION_RADIUS = 4.0;
    private static final double ATTACK_START_DISTANCE = 4.0;
    private LivingEntity targetEnemy = null;

    // Nube invisible solo para las texturas del mod
    private AreaEffectCloud invisibleCloud = null;

    public MushroomGolemGasAttackGoal(MushroomGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity currentTarget = golem.getTarget();
        if (currentTarget instanceof Enemy && currentTarget.isAlive()) {
            this.targetEnemy = currentTarget;
            return !golem.isRetreating();
        }
        this.targetEnemy = findNearestEnemy();
        return this.targetEnemy != null && !golem.isRetreating();
    }

    @Override
    public void start() {
        gasDuration = 0;
        enemyExposureTime.clear();
        golem.setGasReleasing(false);
        createInvisibleCloud();
    }

    @Override
    public boolean canContinueToUse() {
        if (golem.countNearbyEnemies(ENEMY_DETECTION_RADIUS) == 0) {
            return false;
        }
        if (this.targetEnemy == null || !this.targetEnemy.isAlive()) {
            LivingEntity currentTarget = golem.getTarget();
            if (currentTarget instanceof Enemy && currentTarget.isAlive()) {
                this.targetEnemy = currentTarget;
            } else {
                this.targetEnemy = findNearestEnemy();
                if (this.targetEnemy == null) {
                    return false;
                }
            }
        }
        return gasDuration < GAS_TOTAL_DURATION && !golem.isRetreating();
    }

    @Override
    public void tick() {
        if (this.targetEnemy == null) {
            return;
        }
        double distanceToTarget = golem.distanceTo(this.targetEnemy);
        if (distanceToTarget > ATTACK_START_DISTANCE) {
            golem.getNavigation().moveTo(this.targetEnemy, 1.0);
            golem.getLookControl().setLookAt(this.targetEnemy, 30.0F, 30.0F);
            golem.setGasReleasing(false);
            removeInvisibleCloud();
        } else {
            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(this.targetEnemy, 30.0F, 30.0F);
            golem.setGasReleasing(true);
            gasDuration++;

            // Actualizar posición de la nube invisible
            updateInvisibleCloud();

            // Crear partículas (sin las EFFECT blancas)
            createGasCloud();

            // Aplicar daño original
            applyProgressiveDamage();
        }
    }

    @Override
    public void stop() {
        gasDuration = 0;
        enemyExposureTime.clear();
        golem.setGasReleasing(false);
        golem.getNavigation().stop();
        removeInvisibleCloud();
        this.targetEnemy = null;
    }

    private LivingEntity findNearestEnemy() {
        AABB area = new AABB(golem.blockPosition()).inflate(ENEMY_DETECTION_RADIUS);
        List<LivingEntity> enemies = golem.level().getEntitiesOfClass(
                LivingEntity.class, area,
                entity -> entity instanceof Enemy && entity.isAlive());
        if (enemies.isEmpty()) {
            return null;
        }
        return enemies.stream()
                .min((e1, e2) -> Double.compare(golem.distanceTo(e1), golem.distanceTo(e2)))
                .orElse(null);
    }

    // CORREGIDO: Usar EMPTY en lugar de POISON para no auto-envenenar
    private void createInvisibleCloud() {
        if (golem.level() instanceof ServerLevel serverLevel) {
            invisibleCloud = new AreaEffectCloud(golem.level(), golem.getX(), golem.getY(), golem.getZ());
            invisibleCloud.setRadius((float) GAS_RADIUS);
            invisibleCloud.setDuration(GAS_TOTAL_DURATION);
            invisibleCloud.setRadiusPerTick(0.0f);
            invisibleCloud.setWaitTime(0);

            // Usar EMPTY para que no aplique efectos automáticamente
            invisibleCloud.setPotion(Potions.EMPTY);

            // Color morado
            invisibleCloud.setFixedColor(0x7F4F9F);

            // Asegurar que NO aplique efectos
            invisibleCloud.setRadiusOnUse(0.0f);
            invisibleCloud.setRadiusPerTick(0.0f);
            invisibleCloud.setWaitTime(10);

            serverLevel.addFreshEntity(invisibleCloud);
        }
    }

    private void updateInvisibleCloud() {
        if (invisibleCloud != null && invisibleCloud.isAlive()) {
            invisibleCloud.setPos(golem.getX(), golem.getY(), golem.getZ());
        } else if (golem.isGasReleasing()) {
            createInvisibleCloud();
        }
    }

    private void removeInvisibleCloud() {
        if (invisibleCloud != null && invisibleCloud.isAlive()) {
            invisibleCloud.discard();
            invisibleCloud = null;
        }
    }

    // SOLO partículas de micelio (eliminadas las EFFECT blancas)
    private void createGasCloud() {
        if (golem.level() instanceof ServerLevel serverLevel) {
            // Partículas de micelio
            for (int i = 0; i < 2; i++) {
                double offsetX = (golem.getRandom().nextDouble() - 0.5) * 3.5;
                double offsetY = golem.getRandom().nextDouble() * 0.5;
                double offsetZ = (golem.getRandom().nextDouble() - 0.5) * 3.5;

                serverLevel.sendParticles(ParticleTypes.MYCELIUM,
                        golem.getX() + offsetX,
                        golem.getY() + offsetY,
                        golem.getZ() + offsetZ,
                        1,
                        0.1, 0.1, 0.1,
                        0.02);
            }
        }
    }

    private void applyProgressiveDamage() {
        AABB gasArea = new AABB(golem.blockPosition()).inflate(GAS_RADIUS);
        List<LivingEntity> enemies = golem.level().getEntitiesOfClass(
                LivingEntity.class, gasArea,
                entity -> entity.isAlive() && isValidTarget(entity));

        for (LivingEntity enemy : enemies) {
            UUID id = enemy.getUUID();
            int exposureTime = enemyExposureTime.getOrDefault(id, 0) + 1;
            enemyExposureTime.put(id, exposureTime);

            if (gasDuration % 20 == 0) {
                float damage;
                if (exposureTime <= 60) {
                    damage = 3.0f;
                } else if (exposureTime <= 140) {
                    damage = 6.0f;
                } else {
                    damage = 12.0f;
                }
                enemy.hurt(golem.damageSources().magic(), damage);

                int weaknessLevel = Math.min(2, exposureTime / 40);
                enemy.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 80, weaknessLevel, true, true));

                int poisonLevel = Math.min(1, exposureTime / 60);
                enemy.addEffect(new MobEffectInstance(
                        MobEffects.POISON, 60, poisonLevel, true, true));
            }
        }

        enemyExposureTime.keySet().removeIf(id ->
                enemies.stream().noneMatch(e -> e.getUUID().equals(id)));
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == golem) {
            return false;
        }

        if (golem.getOwner() != null && entity == golem.getOwner()) {
            return false;
        }

        if (entity instanceof MushroomGolemEntity) {
            return false;
        }

        if (entity instanceof TamableAnimal tamable) {
            if (tamable.getOwner() != null && tamable.getOwner() == golem.getOwner()) {
                return false;
            }
            return true;
        }

        if (entity instanceof Enemy) {
            return true;
        }

        return false;
    }
}
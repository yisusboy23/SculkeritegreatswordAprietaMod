package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class OxicopperGolemThunderGoal extends Goal {

    private final OxicopperGolemEntity golem;
    private final double detectionRange;
    private final int maxTargets;

    public OxicopperGolemThunderGoal(OxicopperGolemEntity golem, double detectionRange, int maxTargets) {
        this.golem = golem;
        this.detectionRange = detectionRange;
        this.maxTargets = maxTargets;
        // BLOQUEA movimiento y mirada durante el ataque de trueno
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Solo funciona cuando está lloviendo
        if (!this.golem.level().isRaining()) {
            return false;
        }

        // No usar si ya está ejecutando el ataque o está en cooldown
        if (!this.golem.canUseThunder()) {
            return false;
        }

        // Verificar si hay enemigos cerca
        List<LivingEntity> nearbyEnemies = this.getNearbyEnemies();
        if (nearbyEnemies.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Continuar hasta que termine la animación
        return this.golem.isUsingThunder();
    }

    @Override
    public void start() {
        // DETENER TODO MOVIMIENTO
        this.golem.getNavigation().stop();

        // Obtener enemigos cercanos (máximo 3)
        List<LivingEntity> targets = this.getNearbyEnemies();
        if (targets.size() > this.maxTargets) {
            targets = targets.subList(0, this.maxTargets);
        }

        // Iniciar secuencia de ataque de trueno
        this.golem.startThunderAttack(targets);
    }

    @Override
    public void stop() {
        // El golem ya maneja el final del ataque
    }

    @Override
    public void tick() {
        // FORZAR QUEDARSE QUIETO durante el ataque
        this.golem.getNavigation().stop();

        // Mirar al primer objetivo si existe
        List<LivingEntity> currentTargets = this.golem.getThunderTargets();
        if (!currentTargets.isEmpty() && currentTargets.get(0).isAlive()) {
            this.golem.getLookControl().setLookAt(currentTargets.get(0), 30.0F, 30.0F);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // IMPORTANTE: actualizar cada tick
    }

    /**
     * Obtiene lista de enemigos cercanos válidos, ordenados por distancia
     */
    private List<LivingEntity> getNearbyEnemies() {
        AABB detectionBox = this.golem.getBoundingBox().inflate(this.detectionRange);

        List<LivingEntity> enemies = this.golem.level().getEntitiesOfClass(LivingEntity.class, detectionBox, entity -> {
            // No atacarse a sí mismo
            if (entity == this.golem) {
                return false;
            }

            // No atacar aldeanos
            if (entity.getType().toString().toLowerCase().contains("villager")) {
                return false;
            }

            // No atacar al dueño
            if (this.golem.getOwner() != null && entity instanceof Player player && player == this.golem.getOwner()) {
                return false;
            }

            // No atacar otras mascotas del dueño
            if (entity instanceof TamableAnimal other && other.isOwnedBy(this.golem.getOwner())) {
                return false;
            }

            // Verificar si puede atacar
            return this.golem.canAttack(entity);
        });

        // Ordenar por distancia (más cercano primero)
        enemies.sort((e1, e2) -> {
            double dist1 = this.golem.distanceToSqr(e1);
            double dist2 = this.golem.distanceToSqr(e2);
            return Double.compare(dist1, dist2);
        });

        return enemies;
    }
}
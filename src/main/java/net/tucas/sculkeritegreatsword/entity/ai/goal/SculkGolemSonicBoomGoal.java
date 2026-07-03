package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;

import java.util.EnumSet;
import java.util.List;

public class SculkGolemSonicBoomGoal extends Goal {

    private final SculkGolemEntity golem;
    private final double detectionRange;
    private final int cooldownTicks;

    public SculkGolemSonicBoomGoal(SculkGolemEntity golem, double detectionRange, int cooldownTicks) {
        this.golem = golem;
        this.detectionRange = detectionRange;
        this.cooldownTicks = cooldownTicks;
        // BLOQUEA movimiento y mirada durante sonic boom
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // No usar si ya está atacando o usando sonic boom
        if (!this.golem.canUseSonicBoom()) {
            return false;
        }

        // Verificar condición 1: 3+ enemigos cerca
        List<LivingEntity> nearbyEnemies = this.getNearbyEnemies();
        if (nearbyEnemies.size() >= 3) {
            System.out.println("DEBUG: Activando Sonic Boom - 3+ enemigos detectados (" + nearbyEnemies.size() + ")");
            return true;
        }

        // Verificar condición 2: Enemigo actual con 2+ ataques
        LivingEntity target = this.golem.getTarget();
        if (target != null && target.isAlive()) {
            int attackCount = this.golem.getAttackCountForEnemy(target.getUUID());
            if (attackCount >= 2) {
                System.out.println("DEBUG: Activando Sonic Boom - Enemigo resistió " + attackCount + " ataques");
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // Continuar hasta que termine la animación
        return this.golem.isUsingSonicBoom();
    }

    @Override
    public void start() {
        System.out.println("DEBUG: INICIANDO Sonic Boom Goal");

        // DETENER TODO MOVIMIENTO
        this.golem.getNavigation().stop();

        // Iniciar animación de sonic boom
        this.golem.startSonicBoomSequence();

        // Establecer cooldown
        this.golem.getPersistentData().putInt("sonicCooldown", this.cooldownTicks);
    }

    @Override
    public void stop() {
        System.out.println("DEBUG: TERMINANDO Sonic Boom Goal");
    }

    @Override
    public void tick() {
        // FORZAR QUEDARSE QUIETO
        this.golem.getNavigation().stop();

        // Mirar al objetivo si existe
        LivingEntity target = this.golem.getTarget();
        if (target != null && target.isAlive()) {
            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // IMPORTANTE: actualizar cada tick
    }

    /**
     * Obtiene lista de enemigos cercanos válidos
     */
    private List<LivingEntity> getNearbyEnemies() {
        AABB detectionBox = this.golem.getBoundingBox().inflate(this.detectionRange);

        return this.golem.level().getEntitiesOfClass(LivingEntity.class, detectionBox, entity -> {
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
    }
}
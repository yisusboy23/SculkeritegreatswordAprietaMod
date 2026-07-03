package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.ChorusGolemEntity;

import java.util.EnumSet;
import java.util.List;

public class ChorusGolemTeleportAttackGoal extends Goal {

    private final ChorusGolemEntity golem;
    private final double detectionRange;
    private final int cooldownTicks;
    private LivingEntity currentTarget;
    private int attackCounter;

    public ChorusGolemTeleportAttackGoal(ChorusGolemEntity golem, double detectionRange, int cooldownTicks) {
        this.golem = golem;
        this.detectionRange = detectionRange;
        this.cooldownTicks = cooldownTicks;
        this.attackCounter = 0;
        // BLOQUEA movimiento y mirada durante teleport attack
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Verificar cooldown
        int currentCooldown = this.golem.getPersistentData().getInt("teleportCooldown");
        if (currentCooldown > 0) {
            return false;
        }

        // Verificar condición 1: 3+ enemigos cerca
        List<LivingEntity> nearbyEnemies = this.getNearbyEnemies();
        if (nearbyEnemies.size() >= 3) {
            System.out.println("DEBUG: Activando Teleport Attack - 3+ enemigos detectados (" + nearbyEnemies.size() + ")");
            return true;
        }

        // Verificar condición 2: Enemigo actual aguantó 2+ golpes
        LivingEntity target = this.golem.getTarget();
        if (target != null && target.isAlive()) {
            // Contar ataques a este enemigo
            if (this.currentTarget != target) {
                this.currentTarget = target;
                this.attackCounter = 0;
            }

            // Incrementar si el golem está atacando
            if (this.golem.isAttacking()) {
                this.attackCounter++;
            }

            if (this.attackCounter >= 2) {
                System.out.println("DEBUG: Activando Teleport Attack - Enemigo aguantó " + this.attackCounter + " golpes");
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // Continuar hasta que termine la animación
        return this.golem.getPersistentData().getBoolean("isTeleporting");
    }

    @Override
    public void start() {
        System.out.println("DEBUG: INICIANDO Teleport Attack Goal");

        // DETENER TODO MOVIMIENTO
        this.golem.getNavigation().stop();

        // Marcar que está teletransportando
        this.golem.getPersistentData().putBoolean("isTeleporting", true);
        this.golem.getPersistentData().putInt("teleportAnimTicks", 30); // 1.5 segundos

        // Trigger animación teleport_attack
        if (this.golem.level() instanceof net.minecraft.server.level.ServerLevel) {
            this.golem.triggerAnim("main", "teleport_attack");
        }

        // Reproducir sonido de teleport
        this.golem.playSound(
                net.minecraft.sounds.SoundEvents.CHORUS_FRUIT_TELEPORT,
                1.0F,
                1.0F
        );

        // Establecer cooldown
        this.golem.getPersistentData().putInt("teleportCooldown", this.cooldownTicks);

        // Resetear contador de ataques
        this.attackCounter = 0;
    }

    @Override
    public void stop() {
        System.out.println("DEBUG: TERMINANDO Teleport Attack Goal");
        this.golem.getPersistentData().putBoolean("isTeleporting", false);
    }

    @Override
    public void tick() {
        // FORZAR QUEDARSE QUIETO
        this.golem.getNavigation().stop();

        // Contar ticks de animación
        int animTicks = this.golem.getPersistentData().getInt("teleportAnimTicks");
        if (animTicks > 0) {
            animTicks--;
            this.golem.getPersistentData().putInt("teleportAnimTicks", animTicks);

            // A mitad de la animación (tick 15 de 30), ejecutar el ataque
            if (animTicks == 15) {
                this.executeTeleportAttack();
            }

            // Al terminar, limpiar estado
            if (animTicks == 0) {
                this.golem.getPersistentData().putBoolean("isTeleporting", false);
            }
        }

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
     * Ejecuta el ataque de teletransporte con levitación
     */
    private void executeTeleportAttack() {
        List<LivingEntity> nearbyEnemies = this.getNearbyEnemies();

        System.out.println("DEBUG: Ejecutando Teleport Attack en " + nearbyEnemies.size() + " enemigos");

        for (LivingEntity enemy : nearbyEnemies) {
            // Aplicar efecto de levitación (más fuerte que el shulker)
            // Shulker usa nivel 0, nosotros usamos nivel 3 para levitar más rápido
            MobEffectInstance levitation = new MobEffectInstance(
                    MobEffects.LEVITATION,
                    60, // 3 segundos de duración
                    3,  // Nivel 3 (4x más rápido que shulker)
                    false,
                    true
            );
            enemy.addEffect(levitation);

            // Pequeño daño inicial
            enemy.hurt(
                    this.golem.damageSources().mobAttack(this.golem),
                    4.0f
            );

            System.out.println("DEBUG: Aplicado Levitation III a " + enemy.getName().getString());
        }

        // Partículas de portal alrededor del golem
        if (!this.golem.level().isClientSide) {
            for (int i = 0; i < 32; i++) {
                this.golem.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.PORTAL,
                        this.golem.getX() + (this.golem.getRandom().nextDouble() - 0.5) * 2,
                        this.golem.getY() + this.golem.getRandom().nextDouble() * 2,
                        this.golem.getZ() + (this.golem.getRandom().nextDouble() - 0.5) * 2,
                        (this.golem.getRandom().nextDouble() - 0.5) * 0.5,
                        -this.golem.getRandom().nextDouble(),
                        (this.golem.getRandom().nextDouble() - 0.5) * 0.5
                );
            }
        }
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
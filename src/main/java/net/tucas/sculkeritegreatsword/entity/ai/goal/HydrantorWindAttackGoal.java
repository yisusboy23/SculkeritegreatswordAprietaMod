package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.HydrantorGolemEntity;

import java.util.EnumSet;
import java.util.List;

public class HydrantorWindAttackGoal extends Goal {

    private final HydrantorGolemEntity golem;
    private final double detectionRange;
    private final int cooldownTicks;
    private LivingEntity currentTarget;
    private int attackCounter;

    public HydrantorWindAttackGoal(HydrantorGolemEntity golem, double detectionRange, int cooldownTicks) {
        this.golem = golem;
        this.detectionRange = detectionRange;
        this.cooldownTicks = cooldownTicks;
        this.attackCounter = 0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Verificar cooldown
        int currentCooldown = this.golem.getPersistentData().getInt("windAttackCooldown");
        if (currentCooldown > 0) {
            return false;
        }

        // Condición 1: 2+ enemigos cerca
        List<LivingEntity> nearbyEnemies = this.getNearbyEnemies();
        if (nearbyEnemies.size() >= 2) {
            System.out.println("DEBUG: Activando Wind Attack - 2+ enemigos detectados (" + nearbyEnemies.size() + ")");
            return true;
        }

        // Condición 2: Enemigo actual aguantó 3+ golpes
        LivingEntity target = this.golem.getTarget();
        if (target != null && target.isAlive()) {
            if (this.currentTarget != target) {
                this.currentTarget = target;
                this.attackCounter = 0;
            }

            if (this.golem.isAttacking()) {
                this.attackCounter++;
            }

            if (this.attackCounter >= 3) {
                System.out.println("DEBUG: Activando Wind Attack - Enemigo aguantó " + this.attackCounter + " golpes");
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.golem.getPersistentData().getBoolean("isUsingWind");
    }

    @Override
    public void start() {
        System.out.println("DEBUG: INICIANDO Wind Attack Goal");

        this.golem.getNavigation().stop();
        this.golem.getPersistentData().putBoolean("isUsingWind", true);
        this.golem.getPersistentData().putInt("windAnimTicks", 40); // 2 segundos
        this.golem.setUsingWindAttack(true);

        // Trigger animación attack_wind
        if (this.golem.level() instanceof net.minecraft.server.level.ServerLevel) {
            this.golem.triggerAnim("main", "attack_wind");
        }

        // Sonido de viento
        this.golem.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.5F, 0.8F);

        // Establecer cooldown
        this.golem.getPersistentData().putInt("windAttackCooldown", this.cooldownTicks);
        this.attackCounter = 0;
    }

    @Override
    public void stop() {
        System.out.println("DEBUG: TERMINANDO Wind Attack Goal");
        this.golem.getPersistentData().putBoolean("isUsingWind", false);
        this.golem.setUsingWindAttack(false);
    }

    @Override
    public void tick() {
        this.golem.getNavigation().stop();

        int animTicks = this.golem.getPersistentData().getInt("windAnimTicks");
        if (animTicks > 0) {
            animTicks--;
            this.golem.getPersistentData().putInt("windAnimTicks", animTicks);

            // A mitad de la animación (tick 20 de 40), ejecutar el ataque
            if (animTicks == 20) {
                this.executeWindAttack();
            }

            // Partículas durante toda la animación
            if (animTicks > 20 && animTicks % 2 == 0) {
                this.spawnWindParticles();
            }

            if (animTicks == 0) {
                this.golem.getPersistentData().putBoolean("isUsingWind", false);
                this.golem.setUsingWindAttack(false);
            }
        }

        LivingEntity target = this.golem.getTarget();
        if (target != null && target.isAlive()) {
            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Ejecuta el ataque de viento/tornado
     */
    private void executeWindAttack() {
        List<LivingEntity> nearbyEnemies = this.getNearbyEnemies();

        System.out.println("DEBUG: Ejecutando Wind Attack en " + nearbyEnemies.size() + " enemigos");

        Vec3 golemPos = this.golem.position();

        for (LivingEntity enemy : nearbyEnemies) {
            // Calcular dirección desde el golem hacia el enemigo
            Vec3 enemyPos = enemy.position();
            Vec3 direction = enemyPos.subtract(golemPos).normalize();

            // Knockback fuerte hacia afuera
            double knockbackStrength = 2.5;
            Vec3 knockback = direction.scale(knockbackStrength);

            // Añadir componente vertical para lanzar al aire
            knockback = knockback.add(0, 0.8, 0);

            enemy.setDeltaMovement(enemy.getDeltaMovement().add(knockback));
            enemy.hurtMarked = true;

            // Aplicar daño
            enemy.hurt(
                    this.golem.damageSources().mobAttack(this.golem),
                    8.0f
            );

            // Efecto de lentitud temporal
            MobEffectInstance slowness = new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    40, // 2 segundos
                    1,  // Nivel 1
                    false,
                    true
            );
            enemy.addEffect(slowness);

            System.out.println("DEBUG: Aplicado Wind Attack a " + enemy.getName().getString());
        }

        // Explosión de partículas de viento
        this.spawnWindExplosion();

        // Sonido de impacto
        this.golem.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.2F);
    }

    /**
     * Genera partículas de viento durante la carga del ataque
     */
    private void spawnWindParticles() {
        if (this.golem.level().isClientSide) return;

        for (int i = 0; i < 5; i++) {
            double offsetX = (this.golem.getRandom().nextDouble() - 0.5) * 3;
            double offsetY = this.golem.getRandom().nextDouble() * 2;
            double offsetZ = (this.golem.getRandom().nextDouble() - 0.5) * 3;

            this.golem.level().addParticle(
                    ParticleTypes.CLOUD,
                    this.golem.getX() + offsetX,
                    this.golem.getY() + offsetY + 1,
                    this.golem.getZ() + offsetZ,
                    0, 0.05, 0
            );
        }
    }

    /**
     * Genera una explosión de partículas cuando el ataque impacta
     */
    private void spawnWindExplosion() {
        if (this.golem.level().isClientSide) return;

        // Partículas en círculo alrededor del golem
        for (int i = 0; i < 50; i++) {
            double angle = (2 * Math.PI * i) / 50;
            double radius = this.detectionRange * 0.8;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            this.golem.level().addParticle(
                    ParticleTypes.SWEEP_ATTACK,
                    this.golem.getX() + offsetX,
                    this.golem.getY() + 1,
                    this.golem.getZ() + offsetZ,
                    0, 0, 0
            );

            this.golem.level().addParticle(
                    ParticleTypes.CLOUD,
                    this.golem.getX() + offsetX,
                    this.golem.getY() + 1,
                    this.golem.getZ() + offsetZ,
                    offsetX * 0.1, 0.2, offsetZ * 0.1
            );
        }
    }

    /**
     * Obtiene lista de enemigos cercanos válidos
     */
    private List<LivingEntity> getNearbyEnemies() {
        AABB detectionBox = this.golem.getBoundingBox().inflate(this.detectionRange);

        return this.golem.level().getEntitiesOfClass(LivingEntity.class, detectionBox, entity -> {
            if (entity == this.golem) {
                return false;
            }

            if (entity.getType().toString().toLowerCase().contains("villager")) {
                return false;
            }

            if (this.golem.getOwner() != null && entity instanceof Player player && player == this.golem.getOwner()) {
                return false;
            }

            if (entity instanceof TamableAnimal other && other.isOwnedBy(this.golem.getOwner())) {
                return false;
            }

            return this.golem.canAttack(entity);
        });
    }
}
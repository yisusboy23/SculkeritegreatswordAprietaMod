package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tucas.sculkeritegreatsword.entity.custom.DiamondGolemEntity;
import net.tucas.sculkeritegreatsword.entity.projectile.DiamondFrostProjectileEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

import java.util.EnumSet;

public class DiamondGolemProyectileAttackGoal extends Goal {
    private final DiamondGolemEntity golem;
    private final double maxDistance;
    private final double minDistance;
    private int attackCooldown = 0;
    private static final int ATTACK_INTERVAL = 60; // 3 segundos entre ataques (ajustable)
    private static final int PROJECTILE_SPAWN_DELAY = 15; // 0.75 segundos después de iniciar la animación

    public DiamondGolemProyectileAttackGoal(DiamondGolemEntity golem, double maxDistance, double minDistance) {
        this.golem = golem;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        double distSq = this.golem.distanceToSqr(target);

        // Solo atacar a distancia si está en el rango correcto
        return distSq >= (this.minDistance * this.minDistance)
                && distSq <= (this.maxDistance * this.maxDistance);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.golem.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Continuar si estamos en cooldown de ataque o en rango
        return this.attackCooldown > 0 || this.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.attackCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.golem.getTarget();
        if (target == null) {
            return;
        }

        // Siempre mirar al objetivo
        this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Si estamos en cooldown, solo decrementar
        if (this.attackCooldown > 0) {
            this.attackCooldown--;

            // Spawnar proyectil en el momento correcto de la animación
            if (this.attackCooldown == ATTACK_INTERVAL - PROJECTILE_SPAWN_DELAY) {
                this.spawnProjectiles(target);
            }
            return;
        }

        // Verificar si podemos atacar
        double distSq = this.golem.distanceToSqr(target);
        if (distSq >= (this.minDistance * this.minDistance)
                && distSq <= (this.maxDistance * this.maxDistance)) {

            // Iniciar ataque
            this.golem.performProjectileAttack();
            this.attackCooldown = ATTACK_INTERVAL;
        }
    }

    private void spawnProjectiles(LivingEntity target) {
        if (!(this.golem.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Posición de spawn del proyectil
        double spawnX = this.golem.getX();
        double spawnY = this.golem.getY() + this.golem.getEyeHeight() * 0.8;
        double spawnZ = this.golem.getZ();

        // Calcular dirección base al objetivo
        double dx = target.getX() - spawnX;
        double dy = target.getEyeY() - spawnY;
        double dz = target.getZ() - spawnZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        // Normalizar la dirección horizontal
        double normalizedDx = dx / distance;
        double normalizedDz = dz / distance;

        // Ángulo de dispersión (en radianes) - ajusta este valor para cambiar el abanico
        double spreadAngle = Math.toRadians(15); // 15 grados a cada lado

        // Disparar 3 proyectiles
        for (int i = 0; i < 3; i++) {
            DiamondFrostProjectileEntity projectile = new DiamondFrostProjectileEntity(
                    ModEntities.DIAMOND_FROST_PROJECTILE.get(),
                    this.golem,
                    this.golem.level()
            );

            projectile.setPos(spawnX, spawnY, spawnZ);

            // Calcular ángulo para este proyectil
            double angle = 0;
            if (i == 0) {
                angle = -spreadAngle; // Izquierda
            } else if (i == 2) {
                angle = spreadAngle; // Derecha
            }
            // i == 1 se queda con angle = 0 (centro)

            // Aplicar rotación al vector de dirección
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double rotatedDx = normalizedDx * cos - normalizedDz * sin;
            double rotatedDz = normalizedDx * sin + normalizedDz * cos;

            // Escalar de vuelta a la distancia original
            rotatedDx *= distance;
            rotatedDz *= distance;

            // Lanzar el proyectil con la dirección ajustada
            projectile.shoot(rotatedDx, dy + distance * 0.1, rotatedDz, 1.2F, 0.5F);

            // Añadir el proyectil al mundo
            serverLevel.addFreshEntity(projectile);
        }

        // Sonido de disparo
        this.golem.level().playSound(null,
                this.golem.getX(), this.golem.getY(), this.golem.getZ(),
                SoundEvents.SNOW_GOLEM_SHOOT,
                this.golem.getSoundSource(),
                1.0f, 0.8f);
    }

    @Override
    public void stop() {
        super.stop();
        this.attackCooldown = 0;
    }
}
package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.custom.LapisGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModParticles;

import java.util.List;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class LapisGolemCombatGoal extends Goal {
    private final LapisGolemEntity golem;
    private final double detectionRange = 16.0;
    private int attackCooldown = 0;
    private int particleTick = 0;
    private LivingEntity controlledTarget = null;
    private final Map<Mob, Integer> frozenMobs = new HashMap<>();

    public LapisGolemCombatGoal(LapisGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!golem.isTame()) {
            return false;
        }

        List<Mob> nearbyEnemies = getNearbyEnemies();
        return !nearbyEnemies.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return !getNearbyEnemies().isEmpty();
    }

    @Override
    public void start() {
        attackCooldown = 0;
    }

    @Override
    public void tick() {
        List<Mob> enemies = getNearbyEnemies();

        if (enemies.isEmpty()) {
            return;
        }

        // Actualizar mobs congelados
        frozenMobs.entrySet().removeIf(entry -> {
            Mob mob = entry.getKey();
            int timeLeft = entry.getValue() - 1;

            if (timeLeft <= 0 || !mob.isAlive()) {
                mob.setNoAi(false);
                return true;
            }

            // Partículas en enemigos congelados
            if (golem.level() instanceof ServerLevel serverLevel) {
                spawnControlParticles(serverLevel, mob);
            }

            entry.setValue(timeLeft);
            return false;
        });

        // Sistema de cooldown
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Ejecutar ataque cada 5 segundos
        if (attackCooldown == 0) {
            boolean chooseA = golem.getRandom().nextBoolean();

            if (chooseA) {
                performControlAttack(enemies);
            } else {
                performSupportAttack();
            }

            attackCooldown = 100;
        }

        // Partículas en target controlado
        if (particleTick > 0) {
            particleTick--;

            if (controlledTarget != null && controlledTarget.isAlive() && golem.level() instanceof ServerLevel serverLevel) {
                spawnControlParticles(serverLevel, controlledTarget);
            }
        } else {
            controlledTarget = null;
        }
    }

    private void performControlAttack(List<Mob> enemies) {
        golem.startAttackAnimation(); // ✅ CAMBIO AQUÍ
        particleTick = 80;

        if (enemies.size() == 1) {
            // Inmovilización total
            Mob target = enemies.get(0);
            controlledTarget = target;

            target.setNoAi(true);
            frozenMobs.put(target, 80);

            // Explosión inicial de partículas
            if (golem.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; i++) {
                    spawnControlParticles(serverLevel, target);
                }
            }

            golem.lookAt(target, 30.0F, 30.0F);

        } else {
            // 2+ enemigos -> uno ataca a otro
            Mob attacker = enemies.get(golem.getRandom().nextInt(enemies.size()));
            Mob victim = enemies.get(golem.getRandom().nextInt(enemies.size()));

            int attempts = 0;
            while (attacker == victim && attempts < 10) {
                victim = enemies.get(golem.getRandom().nextInt(enemies.size()));
                attempts++;
            }

            if (attacker != victim) {
                controlledTarget = attacker;

                attacker.setTarget(victim);

                // Explosión inicial de partículas en ambos
                if (golem.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 15; i++) {
                        spawnControlParticles(serverLevel, attacker);
                        spawnControlParticles(serverLevel, victim);
                    }
                }

                golem.lookAt(attacker, 30.0F, 30.0F);
            }
        }
    }

    private void performSupportAttack() {
        golem.startAttackAnimation(); // ✅ CAMBIO AQUÍ

        LivingEntity owner = golem.getOwner();
        if (owner == null || !owner.isAlive()) {
            return;
        }

        MobEffectInstance newEffect = getRandomEffect();
        MobEffectInstance existingEffect = owner.getEffect(newEffect.getEffect());

        if (existingEffect != null) {
            int newAmplifier = existingEffect.getAmplifier() + 1;
            owner.addEffect(new MobEffectInstance(
                    newEffect.getEffect(),
                    400,
                    newAmplifier,
                    false,
                    true
            ));
        } else {
            owner.addEffect(newEffect);
        }

        // Partículas de soporte
        if (golem.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (golem.getRandom().nextDouble() - 0.5) * 1.5;
                double offsetY = golem.getRandom().nextDouble() * 2.0;
                double offsetZ = (golem.getRandom().nextDouble() - 0.5) * 1.5;

                serverLevel.sendParticles(
                        ModParticles.LAPIS_CONTROL.get(),
                        owner.getX() + offsetX,
                        owner.getY() + offsetY,
                        owner.getZ() + offsetZ,
                        1, 0, 0.1, 0, 0.05
                );
            }
        }

        golem.lookAt(owner, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        // Reactivar todos los mobs congelados
        frozenMobs.forEach((mob, time) -> {
            if (mob.isAlive()) {
                mob.setNoAi(false);
            }
        });
        frozenMobs.clear();

        controlledTarget = null;
        particleTick = 0;
        attackCooldown = 0;
    }

    private void spawnControlParticles(ServerLevel level, LivingEntity entity) {
        double offsetX = (golem.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
        double offsetY = golem.getRandom().nextDouble() * entity.getBbHeight();
        double offsetZ = (golem.getRandom().nextDouble() - 0.5) * entity.getBbWidth();

        level.sendParticles(
                ModParticles.LAPIS_CONTROL.get(),
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ,
                1,
                0, 0.1, 0,
                0.02
        );
    }

    private List<Mob> getNearbyEnemies() {
        AABB area = new AABB(golem.blockPosition()).inflate(detectionRange);
        return golem.level().getEntitiesOfClass(Mob.class, area,
                mob -> mob instanceof Enemy && mob.isAlive() && mob != golem);
    }

    private MobEffectInstance getRandomEffect() {
        int choice = golem.getRandom().nextInt(5);
        int duration = 400;

        return switch (choice) {
            case 0 -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0);
            case 1 -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0);
            case 2 -> new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0);
            case 3 -> new MobEffectInstance(MobEffects.JUMP, duration, 0);
            default -> new MobEffectInstance(MobEffects.REGENERATION, duration, 0);
        };
    }
}
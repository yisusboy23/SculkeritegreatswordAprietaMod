package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomServantExplodeGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomServantTargetHostilesGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemFollowOwnerGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import java.util.List;

public class MushroomServantEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation ANIM_SPAWN = RawAnimation.begin().thenPlay("spawn");
    // CAMBIA ESTE NOMBRE por el que tengas en tu archivo .geo.json
    // Opciones comunes: "idle", "animation.model.idle", "animation.mushroom.idle"
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ANIM_RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ANIM_EXPLOSION = RawAnimation.begin().thenLoop("explotion");

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MushroomServantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPLOSION_STATE =
            SynchedEntityData.defineId(MushroomServantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_SPAWNED =
            SynchedEntityData.defineId(MushroomServantEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Timer para desaparecer si no encuentra enemigos (30 segundos = 600 ticks)
    private int idleTimer = 0;
    private static final int MAX_IDLE_TIME = 600; // 30 segundos

    public MushroomServantEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
        this.xpReward = 5;
        // Setear attack_hostiles para que los SculkGolem Goals funcionen
        this.getPersistentData().putBoolean("attack_hostiles", true);
        this.getPersistentData().putBoolean("defense", false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D); // ⭐ 15 bloques de rango
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, this.random.nextInt(2));
        this.entityData.define(EXPLOSION_STATE, 0);
        this.entityData.define(HAS_SPAWNED, false);
    }

    @Override
    protected void registerGoals() {
        // Goal de explosión (prioridad máxima)
        this.goalSelector.addGoal(0, new MushroomServantExplodeGoal(this));

        // Seguir al dueño
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false)); // CAMBIO AQUÍ
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, 0.6));

        // Sentarse
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));

        // Caminar aleatoriamente
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));

        // Mirar al jugador
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Mirar alrededor
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // ⭐ TARGETS - UN SOLO GOAL PARA TODOS LOS HOSTILES
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new MushroomServantTargetHostilesGoal(this));
    }



    @Override
    public void tick() {
        super.tick();

        // Animación de spawn solo una vez
        if (!hasSpawned() && this.tickCount == 1) {
            setHasSpawned(true);
        }

        // Sistema de desaparición después de 30 segundos sin enemigos
        if (!this.level().isClientSide && this.isTame()) {
            if (this.getTarget() == null) {
                idleTimer++;

                // Desaparecer después de 30 segundos (600 ticks)
                if (idleTimer >= MAX_IDLE_TIME) {
                    disappearWithSmoke();
                }
            } else {
                // Reiniciar el timer si tiene un objetivo
                idleTimer = 0;
            }
        }
    }

    private void disappearWithSmoke() {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Generar partículas de humo
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = this.random.nextDouble() * 1.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;

                serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1,
                        0.0, 0.1, 0.0,
                        0.0
                );
            }

            // Reproducir sonido
            this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.0F);

            // Eliminar la entidad
            this.discard();
        }
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public int getExplosionState() {
        return this.entityData.get(EXPLOSION_STATE);
    }

    public void setExplosionState(int state) {
        this.entityData.set(EXPLOSION_STATE, state);
    }

    public boolean hasSpawned() {
        return this.entityData.get(HAS_SPAWNED);
    }

    public void setHasSpawned(boolean spawned) {
        this.entityData.set(HAS_SPAWNED, spawned);
    }

    public void explode() {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // ⭐ PARTÍCULAS DE EXPLOSIÓN (VISUAL)
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );

            // Más partículas de explosión
            for (int i = 0; i < 30; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1,
                        0.0, 0.1, 0.0,
                        0.02
                );
            }

            // ⭐ SONIDO DE EXPLOSIÓN
            this.playSound(SoundEvents.GENERIC_EXPLODE, 4.0F, 1.0F);

            // ⭐ BUSCAR ENTIDADES EN EL ÁREA
            net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
                    this.getX() - 3.5, this.getY() - 3.5, this.getZ() - 3.5,
                    this.getX() + 3.5, this.getY() + 3.5, this.getZ() + 3.5
            );

            List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity entity : entities) {
                // ⭐⭐⭐ VERIFICACIONES ESTRICTAS - NO DAÑAR ALIADOS ⭐⭐⭐

                // NO dañar al propio servant
                if (entity == this) {
                    continue;
                }

                // NO dañar a NINGÚN jugador
                if (entity instanceof Player) {
                    continue;
                }

                // NO dañar a NINGUNA mascota domesticada
                if (entity instanceof TamableAnimal) {
                    continue;
                }

                // NO dañar a otros MushroomServants
                if (entity instanceof MushroomServantEntity) {
                    continue;
                }

                // ⭐ SOLO SI PASA TODAS LAS VERIFICACIONES, HACER DAÑO
                double distance = this.distanceTo(entity);
                if (distance <= 3.5) {
                    // ⭐ DAÑO DOBLE: 40.0 en lugar de 20.0
                    float damage = (float) (40.0 * (1.0 - (distance / 3.5)));
                    entity.hurt(this.damageSources().explosion(this, this), damage);
                }
            }

            // Eliminar la entidad
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant());
        tag.putBoolean("HasSpawned", this.hasSpawned());
        tag.putInt("IdleTimer", this.idleTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        }
        if (tag.contains("HasSpawned")) {
            this.setHasSpawned(tag.getBoolean("HasSpawned"));
        }
        if (tag.contains("IdleTimer")) {
            this.idleTimer = tag.getInt("IdleTimer");
        }
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(software.bernie.geckolib.core.animation.AnimationState<MushroomServantEntity> event) {
        // Animación de spawn al aparecer
        if (!hasSpawned()) {
            return event.setAndContinue(ANIM_SPAWN);
        }

        // Animación de explosión
        if (getExplosionState() > 0) {
            return event.setAndContinue(ANIM_EXPLOSION);
        }

        // Animación de correr
        if (event.isMoving() && this.getTarget() != null) {
            return event.setAndContinue(ANIM_RUN);
        }

        // Animación de caminar
        if (event.isMoving()) {
            return event.setAndContinue(ANIM_WALK);
        }

        // Idle
        return event.setAndContinue(ANIM_IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
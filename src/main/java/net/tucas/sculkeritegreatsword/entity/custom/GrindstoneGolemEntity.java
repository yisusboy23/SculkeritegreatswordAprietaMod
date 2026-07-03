package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.tucas.sculkeritegreatsword.entity.ai.goal.GrindstoneGolemRollingAttackGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemFollowOwnerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class GrindstoneGolemEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ROLLING_START = RawAnimation.begin().thenPlay("rolling_start");
    private static final RawAnimation ROLLING = RawAnimation.begin().thenLoop("rolling");
    private static final RawAnimation ROLLING_END = RawAnimation.begin().thenPlay("rolling_end");

    private static final EntityDataAccessor<Boolean> IS_ROLLING =
            SynchedEntityData.defineId(GrindstoneGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ROLLING
    private int rollingTicks = 0;
    private int rollingPhase = 0;
    private LivingEntity rollingTarget = null;
    private int rollingCooldown = 0;
    private static final int ROLLING_COOLDOWN = 120;
    private int stuckTicks = 0;
    private Vec3 lastPos = Vec3.ZERO;

    // MELEE ATTACK
    private int attackAnimationTick = 0;
    private LivingEntity meleeTarget = null;
    private boolean meleeDamageDealt = false;

    public GrindstoneGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.5f);
        this.xpReward = 10;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ROLLING, false);
    }

    public boolean isRolling() {
        return this.entityData.get(IS_ROLLING);
    }

    private void setRolling(boolean rolling) {
        this.entityData.set(IS_ROLLING, rolling);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GrindstoneGolemRollingAttackGoal(this, 16.0, 5.0));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false)); // CAMBIO AQUÍ
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isTame() && this.isOwnedBy(player) && hand == InteractionHand.MAIN_HAND) {
            int currentMode = this.getPersistentData().getInt("golem_mode");
            int newMode = (currentMode % 3) + 1;
            this.getPersistentData().putInt("golem_mode", newMode);

            if (!this.level().isClientSide) {
                String msg = switch(newMode) {
                    case 1 -> "§aFOLLOW MODE";
                    case 2 -> "§eSTAY MODE";
                    case 3 -> "§bWANDER MODE";
                    default -> "";
                };
                player.displayClientMessage(Component.literal(msg), true);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            amount *= 0.5f;
        }

        boolean wasHurt = super.hurt(source, amount);

        if (wasHurt && source.getEntity() instanceof LivingEntity attacker) {
            // Solo contraatacar si no es el dueño
            if (!this.isOwnedBy(attacker)) {
                this.setTarget(attacker);
            }
        }

        return wasHurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!this.isRolling() && target instanceof LivingEntity) {
            this.attackAnimationTick = 25;
            this.meleeTarget = (LivingEntity) target;
            this.meleeDamageDealt = false;

            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "attack");
            }

            return true;
        }

        return super.doHurtTarget(target);
    }

    private void executeMeleeDamage() {
        if (this.meleeTarget != null && this.meleeTarget.isAlive() && !this.meleeDamageDealt) {
            this.meleeTarget.hurt(this.damageSources().mobAttack(this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));

            this.meleeDamageDealt = true;

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.IRON_GOLEM_ATTACK, this.getSoundSource(), 1.0f, 1.0f);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("RollingCooldown", this.rollingCooldown);
        compound.putInt("AttackAnimationTick", this.attackAnimationTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.rollingCooldown = compound.getInt("RollingCooldown");
        this.attackAnimationTick = compound.getInt("AttackAnimationTick");
        this.meleeTarget = null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.rollingCooldown > 0) {
            this.rollingCooldown--;
        }

        // ATAQUE MELEE
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;

            if (this.attackAnimationTick == 13) {
                this.executeMeleeDamage();
            }

            if (this.attackAnimationTick <= 0) {
                this.meleeTarget = null;
                this.meleeDamageDealt = false;
            }
        }

        // ROLLING
        if (this.rollingTicks > 0) {
            this.rollingTicks--;

            if (this.rollingPhase == 1) {
                if (this.rollingTicks <= 0) {
                    this.rollingPhase = 2;
                    this.rollingTicks = 60;
                    this.stuckTicks = 0;
                    this.lastPos = this.position();
                    if (this.level() instanceof ServerLevel) {
                        this.triggerAnim("main", "rolling_loop");
                    }
                }
            } else if (this.rollingPhase == 2) {
                this.tickRollingMovement();
            } else if (this.rollingPhase == 3) {
                if (this.rollingTicks == 13) {
                    this.executeRollingDamage();
                }
                if (this.rollingTicks <= 0) {
                    this.stopRolling();
                }
            }
        }
    }

    private void tickRollingMovement() {
        if (this.rollingTarget == null || !this.rollingTarget.isAlive()) {
            this.stopRolling();
            return;
        }

        // DETECTAR SI ESTÁ TRABADO
        this.stuckTicks++;
        if (this.stuckTicks >= 20) {
            double moved = this.position().distanceTo(this.lastPos);
            if (moved < 0.5) {
                this.stopRolling();
                return;
            }
            this.lastPos = this.position();
            this.stuckTicks = 0;
        }

        // VERIFICAR DISTANCIA
        double distSq = this.distanceToSqr(this.rollingTarget);

        if (distSq <= 4.0) {
            this.rollingPhase = 3;
            this.rollingTicks = 25;
            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "rolling_end");
            }
            return;
        }

        if (distSq > 900.0) {
            this.stopRolling();
            return;
        }

        Vec3 direction = this.rollingTarget.position().subtract(this.position()).normalize();

        // SALTO AUTOMÁTICO para subir bloques
        if (this.horizontalCollision && this.onGround()) {
            this.setDeltaMovement(direction.x * 0.6, 0.5, direction.z * 0.6);
        } else {
            this.setDeltaMovement(direction.x * 0.6, this.getDeltaMovement().y, direction.z * 0.6);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    3, 0.3, 0.1, 0.3, 0.02);
        }
    }

    private void executeRollingDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(1.5);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !(e instanceof GrindstoneGolemEntity) && !isAllyOrOwner(e));

        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().mobAttack(this), 12.0f);

            Vec3 dir = target.position().subtract(this.position()).normalize();
            target.setDeltaMovement(dir.x * 1.0, 0.4, dir.z * 1.0);

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 255, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 255, false, false));
        }

        serverLevel.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.5, this.getZ(),
                20, 1.0, 0.3, 1.0, 0.1);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.IRON_GOLEM_ATTACK, this.getSoundSource(), 1.5f, 0.6f);
    }

    /**
     * Verifica si la entidad es el dueño o una mascota del dueño
     */
    private boolean isAllyOrOwner(LivingEntity entity) {
        // Verificar si es el dueño
        if (this.isOwnedBy(entity)) {
            return true;
        }

        // Verificar si la entidad tiene el mismo dueño (es mascota del mismo jugador)
        if (this.getOwner() != null) {
            // Si es TamableAnimal (perros, gatos, loros, etc.)
            if (entity instanceof TamableAnimal tamable) {
                return tamable.isTame() && tamable.getOwner() == this.getOwner();
            }

            // Si implementa OwnableEntity (otras entidades con dueño)
            if (entity instanceof OwnableEntity ownable) {
                return ownable.getOwner() == this.getOwner();
            }
        }

        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.rollingPhase == 1 || this.rollingPhase == 3 || this.attackAnimationTick > 0) {
            this.getNavigation().stop();
            super.travel(new Vec3(0, travelVector.y, 0));
            return;
        }
        super.travel(travelVector);
    }

    public boolean canUseRolling() {
        return this.rollingCooldown <= 0 && !this.isRolling() && this.attackAnimationTick <= 0;
    }

    public void startRollingAttack(LivingEntity target) {
        this.rollingTarget = target;
        this.rollingPhase = 1;
        this.rollingTicks = 10;
        this.rollingCooldown = ROLLING_COOLDOWN;
        this.setRolling(true);
        this.stuckTicks = 0;
        this.lastPos = this.position();

        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("main", "rolling_start");
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.STONE_FALL, this.getSoundSource(), 1.5f, 0.8f);
    }

    public void stopRolling() {
        this.rollingPhase = 0;
        this.rollingTicks = 0;
        this.rollingTarget = null;
        this.setRolling(false);
        this.stuckTicks = 0;
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.1, 1.0, 0.1));
    }

    private PlayState mainAnimationController(AnimationState<GrindstoneGolemEntity> event) {
        if (this.rollingPhase > 0 || this.attackAnimationTick > 0) {
            return PlayState.CONTINUE;
        }

        if (event.isMoving()) {
            return event.setAndContinue(WALK);
        }

        return event.setAndContinue(IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "main", 5, this::mainAnimationController)
                        .triggerableAnim("attack", ATTACK)
                        .triggerableAnim("rolling_start", ROLLING_START)
                        .triggerableAnim("rolling_loop", ROLLING)
                        .triggerableAnim("rolling_end", ROLLING_END)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }
}
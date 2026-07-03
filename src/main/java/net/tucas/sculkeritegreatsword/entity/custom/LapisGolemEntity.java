package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.entity.ai.goal.LapisGolemCombatGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemFollowOwnerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import net.tucas.sculkeritegreatsword.init.ModParticles;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;

public class LapisGolemEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("attack");

    // ✅ EntityDataAccessor para sincronización cliente-servidor
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(LapisGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTick = 0;
    private static final int ATTACK_ANIMATION_LENGTH = 30; // 1.5 segundos

    public LapisGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false); // ✅ Definir el data accessor
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LapisGolemCombatGoal(this));
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false)); // CAMBIO AQUÍ
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, 0.6));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // 1. CAMBIO DE MODO (click derecho con mano vacía)
        if (this.isTame() && this.isOwnedBy(player) && hand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
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

        // 2. CURACIÓN CON LAPISLÁZULI
        if (this.isTame() && this.isOwnedBy(player)) {
            if (itemStack.is(Items.LAPIS_LAZULI)) {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(5.0f);
                    itemStack.shrink(1);

                    // Partículas azules
                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 10; i++) {
                            serverLevel.sendParticles(ModParticles.LAPIS_CONTROL.get(),
                                    this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                                    this.getY() + this.random.nextDouble() * 2.0,
                                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                                    1, 0, 0.1, 0, 0.02);
                        }
                        for (int i = 0; i < 7; i++) {
                            serverLevel.sendParticles(ParticleTypes.HEART,
                                    this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                                    this.getY() + this.random.nextDouble() * 2.0,
                                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                                    1, 0, 0, 0, 0);
                        }
                    }

                    this.level().playSound(null, this.blockPosition(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, this.getSoundSource(), 1.0F, 1.0F);

                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
        }

        return super.mobInteract(player, hand);
    }


    @Override
    public void tick() {
        super.tick();

        // TICK DE ANIMACIÓN DE ATAQUE
        if (attackAnimationTick > 0) {
            attackAnimationTick--;

            // Partículas durante el ataque
            if (this.level() instanceof ServerLevel serverLevel) {
                spawnAttackParticles(serverLevel);
            }

            // ✅ Desactivar flag cuando termine la animación
            if (attackAnimationTick <= 0) {
                this.entityData.set(IS_ATTACKING, false);
            }
        }
    }

    private void spawnAttackParticles(ServerLevel level) {
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5;
            double offsetY = this.random.nextDouble() * this.getBbHeight();
            double offsetZ = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5;

            level.sendParticles(
                    ModParticles.LAPIS_CONTROL.get(),
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    1,
                    0, 0.05, 0,
                    0.03
            );
        }
    }

    // ✅ Lee desde el EntityDataAccessor (sincronizado)
    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void startAttackAnimation() {
        this.attackAnimationTick = ATTACK_ANIMATION_LENGTH;
        this.entityData.set(IS_ATTACKING, true); // ✅ Activar flag sincronizado

        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("controller", "attack");
        }
    }

    public float getHealthPercentage() {
        return this.getHealth() / this.getMaxHealth();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackAnimationTick", this.attackAnimationTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackAnimationTick = tag.getInt("AttackAnimationTick");
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "controller", 5, this::predicate)
                        .triggerableAnim("attack", ANIM_ATTACK)
        );
    }

    private PlayState predicate(AnimationState<LapisGolemEntity> event) {
        if (this.isAttacking()) {
            return PlayState.CONTINUE;
        }
        if (event.isMoving()) {
            return event.setAndContinue(ANIM_WALK);
        }
        return event.setAndContinue(ANIM_IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
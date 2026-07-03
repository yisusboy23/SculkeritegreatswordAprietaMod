package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.entity.ai.goal.HydrantorWindAttackGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HydrantorGolemEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ATTACK_WIND = RawAnimation.begin().thenPlay("attack_wind");

    // Entity Data Accessors
    public static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(HydrantorGolemEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> USING_WIND_ATTACK =
            SynchedEntityData.defineId(HydrantorGolemEntity.class, EntityDataSerializers.BOOLEAN);

    // Cache de animación
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Attack animation tracking
    // package-private para que HydrantorMeleeAttackGoal pueda accederlo
    int attackAnimationTick = 0;
    private static final int ATTACK_ANIMATION_DURATION = 30; // 1.5 seconds (30 ticks)
    private LivingEntity pendingAttackTarget = null;

    public HydrantorGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.HYDRANTOR_GOLEM.get(), world);
    }

    public HydrantorGolemEntity(EntityType<HydrantorGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 5;
        this.setCanPickUpLoot(false);
        this.setMaxUpStep(0.6f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(USING_WIND_ATTACK, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HydrantorWindAttackGoal(this, 12.0, 300));
        this.goalSelector.addGoal(2, new HydrantorMeleeAttackGoal(this, 1.0, true)); // goal personalizado
        this.goalSelector.addGoal(3, new HydrantorFollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
        this.goalSelector.addGoal(4, new HydrantorWanderGoal(this, 0.8));
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.iron_golem.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.iron_golem.death"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            return this.isTame() && this.isOwnedBy(player) || this.isFood(itemstack) ?
                    InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
        } else {
            if (this.isTame() && this.isOwnedBy(player)) {
                if (!this.isFood(itemstack) && hand == InteractionHand.MAIN_HAND) {
                    int currentMode = this.getPersistentData().getInt("golem_mode");
                    int newMode = (currentMode % 3) + 1;
                    this.getPersistentData().putInt("golem_mode", newMode);

                    String msg = switch(newMode) {
                        case 1 -> "§aFOLLOW MODE";
                        case 2 -> "§eSTAY MODE";
                        case 3 -> "§bWANDER MODE";
                        default -> "";
                    };
                    player.displayClientMessage(Component.literal(msg), true);
                    return InteractionResult.SUCCESS;
                }
                else if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.heal(20.0f);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    return InteractionResult.SUCCESS;
                }
            }
            else if (!this.isTame() && this.isFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.getPersistentData().putInt("golem_mode", 1);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    player.displayClientMessage(Component.literal("§bHydrantor Golem tamed! Mode: FOLLOW"), true);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
        HydrantorGolemEntity baby = ModEntities.HYDRANTOR_GOLEM.get().create(serverWorld);
        if (baby != null) {
            baby.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(baby.blockPosition()),
                    MobSpawnType.BREEDING, null, null);
        }
        return baby;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.IRON_INGOT);
    }

    @Override
    public void tick() {
        super.tick();

        // Decrementar cooldown de wind attack
        int windCooldown = this.getPersistentData().getInt("windAttackCooldown");
        if (windCooldown > 0) {
            this.getPersistentData().putInt("windAttackCooldown", windCooldown - 1);
        }

        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;

            // DAÑO: tick 12 desde inicio (contador en 18)
            if (this.attackAnimationTick == 18 && this.pendingAttackTarget != null) {
                if (this.pendingAttackTarget.isAlive() && this.distanceTo(this.pendingAttackTarget) < 3.5) {
                    super.doHurtTarget(this.pendingAttackTarget);
                }
            }

            // VUELO: tick 14 desde inicio (contador en 16), ~100ms después del daño
            if (this.attackAnimationTick == 16 && this.pendingAttackTarget != null) {
                if (this.pendingAttackTarget.isAlive() && this.distanceTo(this.pendingAttackTarget) < 3.5) {
                    double dx = this.pendingAttackTarget.getX() - this.getX();
                    double dz = this.pendingAttackTarget.getZ() - this.getZ();
                    double length = Math.sqrt(dx * dx + dz * dz);
                    if (length > 0) {
                        dx /= length;
                        dz /= length;
                    }
                    this.pendingAttackTarget.setDeltaMovement(
                            dx * 0.4,
                            0.78, // ≈ 7 bloques de altura
                            dz * 0.4
                    );
                    this.pendingAttackTarget.hasImpulse = true;
                }
                this.pendingAttackTarget = null;
            }

            if (this.attackAnimationTick == 0) {
                this.setAttacking(false);
                this.pendingAttackTarget = null;
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            this.pendingAttackTarget = livingTarget;
            this.triggerMeleeAttackAnimation();
            return true;
        }
        return false;
    }

    public void triggerMeleeAttackAnimation() {
        this.setAttacking(true);
        this.attackAnimationTick = ATTACK_ANIMATION_DURATION;

        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("main", "attack");
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isUsingWindAttack() {
        return this.entityData.get(USING_WIND_ATTACK);
    }

    public void setUsingWindAttack(boolean using) {
        this.entityData.set(USING_WIND_ATTACK, using);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 180.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 2.8);
    }

    private PlayState mainAnimationController(AnimationState<HydrantorGolemEntity> event) {
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
                        .triggerableAnim("attack_wind", ATTACK_WIND)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============= INNER CLASSES PARA GOALS PERSONALIZADOS =============

    /**
     * MeleeAttackGoal personalizado que bloquea SOLO el golpe durante la animación,
     * pero sigue persiguiendo al enemigo normalmente.
     */
    private static class HydrantorMeleeAttackGoal extends MeleeAttackGoal {
        private final HydrantorGolemEntity golem;

        public HydrantorMeleeAttackGoal(HydrantorGolemEntity golem, double speed, boolean followIfNotSeen) {
            super(golem, speed, followIfNotSeen);
            this.golem = golem;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            // Si la animación anterior no terminó, no intenta golpear
            // pero el goal SIGUE caminando hacia el enemigo normalmente
            if (this.golem.attackAnimationTick > 0) {
                return;
            }
            super.checkAndPerformAttack(enemy, distToEnemySqr);
        }
    }

    private static class HydrantorFollowOwnerGoal extends FollowOwnerGoal {
        private final HydrantorGolemEntity golem;

        public HydrantorFollowOwnerGoal(HydrantorGolemEntity golem, double speed, float minDist, float maxDist, boolean canFly) {
            super(golem, speed, minDist, maxDist, canFly);
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 1 && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 1 && super.canContinueToUse();
        }
    }

    private static class HydrantorWanderGoal extends RandomStrollGoal {
        private final HydrantorGolemEntity golem;

        public HydrantorWanderGoal(HydrantorGolemEntity golem, double speed) {
            super(golem, speed);
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 3 && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 3 && super.canContinueToUse();
        }
    }
}
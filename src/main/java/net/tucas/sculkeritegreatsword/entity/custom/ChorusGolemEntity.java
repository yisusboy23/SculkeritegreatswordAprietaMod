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
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
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
import net.tucas.sculkeritegreatsword.entity.ai.goal.ChorusGolemTeleportAttackGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ChorusGolemEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation TELEPORT_ATTACK = RawAnimation.begin().thenPlay("teleport_attack");

    // Entity Data Accessors
    public static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(ChorusGolemEntity.class, EntityDataSerializers.BOOLEAN);

    // Cache de animación
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Attack animation tracking
    private int attackAnimationTick = 0;
    private static final int ATTACK_ANIMATION_DURATION = 20; // 1 second (20 ticks)
    private LivingEntity pendingAttackTarget = null; // Target to attack when animation reaches damage frame

    public ChorusGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.CHORUS_GOLEM.get(), world);
    }

    public ChorusGolemEntity(EntityType<ChorusGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 5;
        this.setCanPickUpLoot(false);
        this.setMaxUpStep(0.6f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ChorusGolemTeleportAttackGoal(this, 8.0, 200)); // 8 bloques de rango, 10 segundos cooldown
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new ChorusGolemFollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
        this.goalSelector.addGoal(4, new ChorusGolemWanderGoal(this, 0.8));
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
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Inmune a daño por caída
        if (source.is(DamageTypes.FALL)) {
            return false;
        }

        // Inmune a proyectiles (flechas, bolas de fuego, tridents, etc.)
        if (source.is(DamageTypes.ARROW) ||
                source.is(DamageTypes.FIREBALL) ||
                source.is(DamageTypes.TRIDENT) ||
                source.is(DamageTypes.WITHER_SKULL) ||
                source.getDirectEntity() != null && source.getDirectEntity() != source.getEntity()) {
            // Si el daño directo es diferente a la entidad que lo causó, es un proyectil
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
            // Si ya está domesticado y es su dueño
            if (this.isTame() && this.isOwnedBy(player)) {
                // Si NO tiene comida en la mano, cambiar modo
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
                // Si tiene comida, curar (opcional - puedes quitar esto si no quieres)
                else if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.heal(20.0f);
                    this.level().broadcastEntityEvent(this, (byte) 7); // Corazones
                    return InteractionResult.SUCCESS;
                }
            }
            // Si NO está domesticado y tiene comida, intentar domesticar
            else if (!this.isTame() && this.isFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.getPersistentData().putInt("golem_mode", 1); // FOLLOW MODE por defecto
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    player.displayClientMessage(Component.literal("§aChorus Golem tamed! Mode: FOLLOW"), true);
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
        ChorusGolemEntity baby = ModEntities.CHORUS_GOLEM.get().create(serverWorld);
        if (baby != null) {
            baby.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(baby.blockPosition()),
                    MobSpawnType.BREEDING, null, null);
        }
        return baby;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CHORUS_FRUIT);
    }

    @Override
    public void tick() {
        super.tick();

        // Decrementar cooldown de teleport
        int teleportCooldown = this.getPersistentData().getInt("teleportCooldown");
        if (teleportCooldown > 0) {
            this.getPersistentData().putInt("teleportCooldown", teleportCooldown - 1);
        }

        // Handle attack animation timing
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;

            // Apply damage at the middle of the animation (tick 10 out of 20)
            // This corresponds to around 0.5 seconds into the animation where the swing happens
            if (this.attackAnimationTick == 10 && this.pendingAttackTarget != null) {
                if (this.pendingAttackTarget.isAlive() && this.distanceTo(this.pendingAttackTarget) < 3.5) {
                    // Apply the actual damage
                    super.doHurtTarget(this.pendingAttackTarget);
                }
                this.pendingAttackTarget = null;
            }

            if (this.attackAnimationTick == 0) {
                this.setAttacking(false);
                this.pendingAttackTarget = null; // Clear target if animation ends
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        // Don't apply damage immediately, store target and trigger animation
        // Damage will be applied at the right moment during the animation
        if (target instanceof LivingEntity livingTarget) {
            this.pendingAttackTarget = livingTarget;
            this.triggerMeleeAttackAnimation();
            return true; // Return true to indicate attack was initiated
        }
        return false;
    }

    public void triggerMeleeAttackAnimation() {
        this.setAttacking(true);
        this.attackAnimationTick = ATTACK_ANIMATION_DURATION;

        // Trigger the animation on the client side
        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("main", "attack");
        }
    }

    // Método helper para verificar si está atacando
    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    // Método para activar el estado de ataque
    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Inmune al retroceso
    }

    // Controlador de animación
    private PlayState mainAnimationController(AnimationState<ChorusGolemEntity> event) {
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
                        .triggerableAnim("teleport_attack", TELEPORT_ATTACK)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============= INNER CLASSES PARA GOALS PERSONALIZADOS =============

    /**
     * Goal personalizado para seguir al dueño (solo en modo FOLLOW)
     */
    private static class ChorusGolemFollowOwnerGoal extends FollowOwnerGoal {
        private final ChorusGolemEntity golem;

        public ChorusGolemFollowOwnerGoal(ChorusGolemEntity golem, double speed, float minDist, float maxDist, boolean canFly) {
            super(golem, speed, minDist, maxDist, canFly);
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 1 && super.canUse(); // Solo activo en FOLLOW MODE
        }

        @Override
        public boolean canContinueToUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 1 && super.canContinueToUse();
        }
    }

    /**
     * Goal personalizado para vagar (solo en modo WANDER)
     */
    private static class ChorusGolemWanderGoal extends RandomStrollGoal {
        private final ChorusGolemEntity golem;

        public ChorusGolemWanderGoal(ChorusGolemEntity golem, double speed) {
            super(golem, speed);
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 3 && super.canUse(); // Solo activo en WANDER MODE
        }

        @Override
        public boolean canContinueToUse() {
            int mode = this.golem.getPersistentData().getInt("golem_mode");
            return mode == 3 && super.canContinueToUse();
        }
    }
}
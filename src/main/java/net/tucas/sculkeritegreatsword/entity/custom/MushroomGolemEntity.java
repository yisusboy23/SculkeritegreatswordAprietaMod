package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.ai.goal.*;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MushroomGolemEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ANIM_RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenLoop("attack");

    public enum Variant {
        RED,
        BROWN
    }

    private static final EntityDataAccessor<Boolean> IS_RETREATING =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GAS_RELEASING =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TAUNTING =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> STORED_POTION =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.INT);

    private MobEffectInstance storedEffect = null;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MushroomGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_RETREATING, false);
        this.entityData.define(IS_GAS_RELEASING, false);
        this.entityData.define(IS_TAUNTING, true);
        this.entityData.define(STORED_POTION, ItemStack.EMPTY);
        this.entityData.define(VARIANT, Variant.RED.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MushroomGolemFleeAndHealGoal(this));
        this.goalSelector.addGoal(1, new MushroomGolemGasAttackGoal(this));
        this.goalSelector.addGoal(2, new MushroomGolemAuraGoal(this));
        this.goalSelector.addGoal(3, new MushroomGolemTauntGoal(this));
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false)); // CAMBIO AQUÍ
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isTame() && this.isOwnedBy(player)) {
            // CAMBIO DE MODO (sin shift, mano vacía)
            if (!player.isShiftKeyDown() && itemStack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
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

            // CURACIÓN CON BONE MEAL
            if (player.isShiftKeyDown() && itemStack.is(Items.BONE_MEAL)) {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(25.0f);
                    itemStack.shrink(1);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 7; i++) {
                            serverLevel.sendParticles(ParticleTypes.HEART,
                                    this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                                    this.getY() + this.random.nextDouble() * 2.0,
                                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                                    1, 0, 0, 0, 0);
                        }
                    }

                    this.level().playSound(null, this.blockPosition(),
                            SoundEvents.BONE_MEAL_USE, this.getSoundSource(), 1.0F, 1.0F);

                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }

            // ALMACENAR/EXTRAER POCIÓN
            if (player.isShiftKeyDown()) {
                if (itemStack.getItem() == Items.POTION && getStoredPotion().isEmpty()) {
                    setStoredPotion(itemStack.copy());
                    extractPotionEffect(itemStack);
                    itemStack.shrink(1);
                    player.addItem(new ItemStack(Items.GLASS_BOTTLE));
                    return InteractionResult.SUCCESS;
                }
                else if (itemStack.is(Items.GLASS_BOTTLE) && !getStoredPotion().isEmpty()) {
                    player.addItem(getStoredPotion().copy());
                    setStoredPotion(ItemStack.EMPTY);
                    storedEffect = null;
                    itemStack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        // DOMESTICAR
        else if (itemStack.is(Items.RED_MUSHROOM) || itemStack.is(Items.BROWN_MUSHROOM)) {
            if (!this.level().isClientSide) {
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private void extractPotionEffect(ItemStack potionStack) {
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);
        if (!effects.isEmpty()) {
            storedEffect = effects.get(0);
        }
    }

    public int countNearbyEnemies(double radius) {
        AABB area = new AABB(this.blockPosition()).inflate(radius);
        List<Mob> enemies = this.level().getEntitiesOfClass(Mob.class, area);
        return (int) enemies.stream()
                .filter(mob -> mob instanceof Enemy && mob.isAlive())
                .count();
    }

    public Variant getVariant() {
        return Variant.values()[this.entityData.get(VARIANT)];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(VARIANT, variant.ordinal());
    }

    public boolean isRetreating() {
        return this.entityData.get(IS_RETREATING);
    }

    public void setRetreating(boolean retreating) {
        this.entityData.set(IS_RETREATING, retreating);
    }

    public boolean isGasReleasing() {
        return this.entityData.get(IS_GAS_RELEASING);
    }

    public void setGasReleasing(boolean releasing) {
        this.entityData.set(IS_GAS_RELEASING, releasing);
    }

    public boolean isTaunting() {
        return this.entityData.get(IS_TAUNTING);
    }

    public void setTaunting(boolean taunting) {
        this.entityData.set(IS_TAUNTING, taunting);
    }

    public boolean hasActivePotion() {
        return storedEffect != null;
    }

    public MobEffectInstance getStoredEffect() {
        return storedEffect;
    }

    public ItemStack getStoredPotion() {
        return this.entityData.get(STORED_POTION);
    }

    public void setStoredPotion(ItemStack potion) {
        this.entityData.set(STORED_POTION, potion);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant().ordinal());
        if (!getStoredPotion().isEmpty()) {
            tag.put("StoredPotion", getStoredPotion().save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.setVariant(Variant.values()[tag.getInt("Variant")]);
        }
        if (tag.contains("StoredPotion")) {
            setStoredPotion(ItemStack.of(tag.getCompound("StoredPotion")));
            extractPotionEffect(getStoredPotion());
        }
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
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<MushroomGolemEntity> event) {
        if (this.isRetreating()) {
            return event.setAndContinue(ANIM_RUN);
        }
        if (this.isGasReleasing()) {
            return event.setAndContinue(ANIM_ATTACK);
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
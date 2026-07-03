// DrillerEntity.java
package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.item.Moditems;
import net.tucas.sculkeritegreatsword.entity.ai.goal.DrillerDigGoal;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

public class DrillerEntity extends Animal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlayAndHold("attack");
    private static final RawAnimation DIG = RawAnimation.begin().thenLoop("dig");

    private static final EntityDataAccessor<Boolean> IS_DIGGING =
            SynchedEntityData.defineId(DrillerEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int digCooldown = 0;

    public DrillerEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_DIGGING, false);
    }

    public boolean isDigging() { return this.entityData.get(IS_DIGGING); }
    public void setDigging(boolean digging) { this.entityData.set(IS_DIGGING, digging); }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && digCooldown > 0) digCooldown--;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DrillerDigGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 18.0);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.PITCHER_PLANT);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {

        if (!level.isClientSide) {
            this.spawnAtLocation(new ItemStack(Moditems.DRILLER_EGG.get()));
        }

        this.setAge(6000);
        mate.setAge(6000);

        this.setInLoveTime(0);
        ((Animal) mate).setInLoveTime(0);

        return null;
    }
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.PITCHER_POD) && digCooldown <= 0 && !isDigging() && !this.isBaby()) {
            setDigging(true);
            digCooldown = 600;
            if (!player.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // ─── 2. Hitbox diferente para bebé / adulto ──────────────────────────────────
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions base = super.getDimensions(pose);
        return this.isBaby() ? base.scale(0.8F) : base;
    }

    // ─── 3. Escala visual GeckoLib ───────────────────────────────────────────────
    @Override
    public float getScale() {
        return this.isBaby() ? 0.5F : 1.0F;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            // Si está excavando y parado, para el movimiento
            if (isDigging() && !state.isMoving()) return PlayState.STOP;
            // Si está caminando (incluso hacia el ore), muestra walk
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));

        registrar.add(new AnimationController<>(this, "attack", 2, state -> {
            if (this.swinging) return state.setAndContinue(ATTACK);
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        }));

        registrar.add(new AnimationController<>(this, "dig", 2, state -> {
            // Solo muestra dig cuando está parado Y excavando
            if (isDigging() && !state.isMoving() && !this.isBaby()) {
                return state.setAndContinue(DIG);
            }
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
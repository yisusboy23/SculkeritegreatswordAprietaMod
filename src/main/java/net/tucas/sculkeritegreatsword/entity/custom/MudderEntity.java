package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MudderBurrowGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MudderEnterWaterGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MudderLeaveWaterGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MudderRandomStrollGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MudderRandomSwimGoal;
import net.tucas.sculkeritegreatsword.entity.ai.navigation.MudderPathNavigation;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.tucas.sculkeritegreatsword.item.Moditems;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.tucas.sculkeritegreatsword.item.Moditems;

public class MudderEntity extends Animal implements GeoEntity, Bucketable {

    // ── Synced Data ──────────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> FROM_BUCKET =
            SynchedEntityData.defineId(MudderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BURROWED =
            SynchedEntityData.defineId(MudderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BURROWING =
            SynchedEntityData.defineId(MudderEntity.class, EntityDataSerializers.BOOLEAN);

    // ── Animation keys — ADULTO ──────────────────────────────────────────────
    private static final RawAnimation ANIM_IDLE         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_WALK         = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ANIM_SWIM         = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation ANIM_SWIM_IDLE    = RawAnimation.begin().thenLoop("swim_idle");
    private static final RawAnimation ANIM_BURROW_START = RawAnimation.begin().thenPlay("burrow_start");
    private static final RawAnimation ANIM_BURROW_IDLE  = RawAnimation.begin().thenLoop("burrow_idle");

    // ── Animation keys — BEBÉ ────────────────────────────────────────────────
    private static final RawAnimation ANIM_BABY_IDLE      = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_BABY_SWIM_IDLE = RawAnimation.begin().thenLoop("swim_idle");
    private static final RawAnimation ANIM_BABY_SWIM_WALK = RawAnimation.begin().thenLoop("swim_walk");

    // ── GeckoLib cache ───────────────────────────────────────────────────────
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ── Navigator state ──────────────────────────────────────────────────────
    private boolean isLandNavigator = true;

    // ── Burrow cooldown ───────────────────────────────────────────────────────
    public int burrowCooldown = 0;
    private Vec3 burrowPos = null;

    public MudderEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(BlockPathTypes.WATER,        0.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 1.0F);
        switchNavigator(true);
    }
    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    protected int increaseAirSupply(int air) { return this.getMaxAirSupply(); }

    // ── Attributes ───────────────────────────────────────────────────────────
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH,     12.0D)
                .add(Attributes.MOVEMENT_SPEED,  0.2D);
    }

    // ── Goals ────────────────────────────────────────────────────────────────
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.4D, 1.6D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.TROPICAL_FISH), false));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new MudderLeaveWaterGoal(this, 1.0D, 1500));
        this.goalSelector.addGoal(3, new MudderEnterWaterGoal(this, 1.0D, 800));
        this.goalSelector.addGoal(4, new MudderBurrowGoal(this));
        this.goalSelector.addGoal(5, new MudderRandomSwimGoal(this, 1.0D, 80));
        this.goalSelector.addGoal(5, new MudderRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // ── Navigator switching ──────────────────────────────────────────────────
    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveControl  = new net.minecraft.world.entity.ai.control.MoveControl(this);
            this.lookControl  = new net.minecraft.world.entity.ai.control.LookControl(this);
            this.navigation   = this.createNavigation(this.level());
            this.isLandNavigator = true;
        } else {
            this.moveControl  = new SmoothSwimmingMoveControl(this, 85, 10, 0.34F, 1.0F, false);
            this.lookControl  = new SmoothSwimmingLookControl(this, 20);
            this.navigation   = new MudderPathNavigation(this, this.level());
            this.isLandNavigator = false;
        }
    }

    // ── Tick ─────────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();

        boolean inWater = this.isInWaterOrBubble();
        if (!inWater && !isLandNavigator) switchNavigator(true);
        if ( inWater &&  isLandNavigator) switchNavigator(false);

        if (burrowCooldown > 0) burrowCooldown--;

        if (this.isBurrowed() && burrowPos != null) {
            this.noPhysics = true;
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(burrowPos.x, burrowPos.y + 0.099, burrowPos.z);
            this.navigation.stop();
        } else {
            this.noPhysics = false;
        }
    }

    @Override
    public void travel(@NotNull Vec3 vec) {
        if (this.isBurrowed()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), vec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(vec);
        }
    }
    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 vec) {
        if (this.isBurrowed()) return;
        super.move(type, vec);
    }

    // ── Burrow helpers ────────────────────────────────────────────────────────
    public boolean isBurrowing() { return this.entityData.get(BURROWING); }
    public boolean isBurrowed()  { return this.entityData.get(BURROWED); }

    public void startBurrow() {
        if (burrowCooldown == 0 && !isInWaterOrBubble() && !isBaby()) {
            this.burrowPos = this.position();
            this.entityData.set(BURROWING, true);
            this.entityData.set(BURROWED,  false);
        }
    }

    public void finishBurrow() {
        this.entityData.set(BURROWING, false);
        this.entityData.set(BURROWED,  true);
    }

    public void stopBurrow() {
        this.entityData.set(BURROWING, false);
        this.entityData.set(BURROWED, false);
        burrowCooldown = 400 + this.random.nextInt(400);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {

            int roll = this.random.nextInt(100);

            if (roll < 40) return; // 40% nada

            ResourceLocation id = (roll < 80)
                    ? new ResourceLocation("sculkeritegreatsword", "entities/mudder_burrow_common")
                    : new ResourceLocation("sculkeritegreatsword", "entities/mudder_burrow_rare");

            var loot = serverLevel.getServer()
                    .getLootData()
                    .getLootTable(id);

            var params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .create(LootContextParamSets.GIFT);

            loot.getRandomItems(params).forEach(this::spawnAtLocation);
        }
    }
    public boolean canBurrowHere() {
        BlockState below   = this.level().getBlockState(this.blockPosition().below());
        BlockState current = this.level().getBlockState(this.blockPosition());
        return below.is(Blocks.MUD)
                || below.is(Blocks.SAND)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.COARSE_DIRT)
                || below.is(Blocks.GRAVEL)
                || current.is(Blocks.MUD);
    }

    // ── Synced data ───────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FROM_BUCKET, false);
        this.entityData.define(BURROWED,    false);
        this.entityData.define(BURROWING,   false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("FromBucket", this.fromBucket());
        tag.putBoolean("Burrowed",   this.isBurrowed());
        tag.putInt("BurrowCooldown", this.burrowCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFromBucket(tag.getBoolean("FromBucket"));
        if (tag.getBoolean("Burrowed")) this.finishBurrow();
        this.burrowCooldown = tag.getInt("BurrowCooldown");
    }

    // ── Bucketable ────────────────────────────────────────────────────────────
    @Override public boolean fromBucket() { return this.entityData.get(FROM_BUCKET); }
    @Override public void setFromBucket(boolean v) { this.entityData.set(FROM_BUCKET, v); }

    @Override
    public @NotNull SoundEvent getPickupSound() { return SoundEvents.BUCKET_FILL_FISH; }

    @Override
    public void saveToBucketTag(@NotNull ItemStack bucket) {
        Bucketable.saveDefaultDataToBucketTag(this, bucket);
        CompoundTag tag = bucket.getOrCreateTag();
        tag.putInt("Age", this.getAge());
        tag.putInt("BurrowCooldown", this.burrowCooldown);
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag tag) {
        Bucketable.loadDefaultDataFromBucketTag(this, tag);
        this.setAge(tag.getInt("Age"));
        this.burrowCooldown = tag.getInt("BurrowCooldown");
    }


    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(Moditems.MUDDER_BUCKET.get());
    }
    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        return Bucketable.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    // ── Sounds ────────────────────────────────────────────────────────────────
    @Nullable @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.COD_AMBIENT; }

    @Nullable @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource src) { return SoundEvents.COD_HURT; }

    @Nullable @Override
    protected SoundEvent getDeathSound() { return SoundEvents.COD_DEATH; }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.15F, 1.0F);
    }

    // ── Breeding ──────────────────────────────────────────────────────────────
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return stack.is(Items.TROPICAL_FISH);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {

        if (!level.isClientSide) {
            this.spawnAtLocation(new ItemStack(Moditems.MUDDER_EGG.get()));
        }
        this.setAge(6000);
        other.setAge(6000);
        this.setInLoveTime(0);
        ((Animal) other).setInLoveTime(0);

        return null;
    }


    // ── Spawn ─────────────────────────────────────────────────────────────────
    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
                                                 @NotNull DifficultyInstance difficulty,
                                                 @NotNull MobSpawnType spawnType,
                                                 @Nullable SpawnGroupData spawnData,
                                                 @Nullable CompoundTag tag) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }

    public static boolean checkSpawnRules(EntityType<MudderEntity> type,
                                          LevelAccessor level,
                                          MobSpawnType spawnType,
                                          BlockPos pos,
                                          RandomSource random) {
        return level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
    }

    // ── GeckoLib ──────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 4, state -> {
            boolean moving  = state.getLimbSwingAmount() > 0.05F;
            boolean inWater = this.isInWaterOrBubble();
            boolean baby    = this.isBaby();

            if (baby) {
                if (inWater) {
                    return moving
                            ? state.setAndContinue(ANIM_BABY_SWIM_WALK)
                            : state.setAndContinue(ANIM_BABY_SWIM_IDLE);
                }
                return state.setAndContinue(ANIM_BABY_IDLE);
            }

            if (isBurrowed())  return state.setAndContinue(ANIM_BURROW_IDLE);
            if (isBurrowing()) return state.setAndContinue(ANIM_BURROW_START);

            if (inWater) {
                return moving
                        ? state.setAndContinue(ANIM_SWIM)
                        : state.setAndContinue(ANIM_SWIM_IDLE);
            }
            return moving
                    ? state.setAndContinue(ANIM_WALK)
                    : state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override
    public boolean isPushable() { return !this.isBurrowed(); }

    @Override
    public void push(@NotNull Entity entity) {
        if (this.isBurrowed()) return;
        super.push(entity);
    }

    @Override
    protected void pushEntities() {
        if (this.isBurrowed()) return;
        super.pushEntities();
    }

    @Override
    public boolean isPickable() { return !this.isBurrowed(); }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if ((this.isBurrowed() || this.isBurrowing()) && amount > 0) {
            this.stopBurrow(); // sale del burrow
        }
        return super.hurt(source, amount);
        // super.hurt() automáticamente setea lastHurtByMob
        // eso activa PanicGoal y AvoidEntityGoal que ya tienes registrados
    }
}
package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.block.PeekerFetusBlock;
import net.tucas.sculkeritegreatsword.entity.ai.goal.PeekerAvoidPlayerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.PeekerPanicGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.PeekerConstants;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.item.Moditems;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Peeker: nace bebé, crece solo, huye del jugador mientras es salvaje,
 * se domestica, se ensilla y se monta. Al morir explota.
 * La textura se calcula a partir del estado (silla / muerte), no se guarda.
 */
public class PeekerEntity extends TamableAnimal implements GeoEntity {

    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(PeekerEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PeekerEntity(EntityType<? extends PeekerEntity> type, Level world) {
        super(type, world);
        this.setSpeed(0.3F);
        this.setMaxUpStep(PeekerConstants.MAX_STEP_HEIGHT);
    }

    public PeekerEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.PEEKER.get(), world);
    }

    // ------------------------------------------------------------------
    // Datos sincronizados / guardado
    // ------------------------------------------------------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SADDLED, false);
    }

    public boolean isSaddled() {
        return this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
    }

    /** Textura derivada del estado. La usa PeekerModel. */
    public String getTexture() {
        if (this.deathTime >= 2) {
            return isSaddled() ? PeekerConstants.TEXTURE_EXPLODE_SADDLED : PeekerConstants.TEXTURE_EXPLODE_WILD;
        }
        return isSaddled() ? PeekerConstants.TEXTURE_SADDLED : PeekerConstants.TEXTURE_WILD;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Saddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Saddled")) setSaddled(compound.getBoolean("Saddled"));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ------------------------------------------------------------------
    // IA
    // ------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PeekerAvoidPlayerGoal(this, PeekerConstants.AVOID_PLAYER_DISTANCE, PeekerConstants.AVOID_PLAYER_SPEED_WALK, PeekerConstants.AVOID_PLAYER_SPEED_SPRINT));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, PeekerConstants.LOOK_AT_PLAYER_RANGE));
        this.goalSelector.addGoal(3, new PeekerPanicGoal(this, PeekerConstants.PANIC_SPEED));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, PeekerConstants.STROLL_SPEED));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, PeekerConstants.MOVEMENT_SPEED)
                .add(Attributes.MAX_HEALTH, PeekerConstants.MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE, PeekerConstants.FOLLOW_RANGE)
                .add(Attributes.ATTACK_KNOCKBACK, PeekerConstants.ATTACK_KNOCKBACK);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    // ------------------------------------------------------------------
    // Sonidos
    // ------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return modSound(this.isBaby() ? PeekerConstants.SOUND_IDLE_BABY : PeekerConstants.SOUND_IDLE);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return modSound(this.isBaby() ? PeekerConstants.SOUND_HURT_BABY : PeekerConstants.SOUND_HURT);
    }

    private SoundEvent modSound(String name) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Sculkeritegreatsword.MOD_ID, name));
    }

    // ------------------------------------------------------------------
    // Domesticación, silla y montura
    // ------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            return tryTame(player, stack);
        }
        if (!this.isSaddled() && stack.is(Items.SADDLE)) {
            return equipSaddle(player, stack);
        }
        if (this.isSaddled() && player.isShiftKeyDown() && stack.isEmpty()) {
            return unequipSaddle();
        }
        if (this.isSaddled() && !this.isVehicle() && hand == InteractionHand.MAIN_HAND && stack.isEmpty()) {
            return mount(player);
        }
        return super.mobInteract(player, hand);
    }

    /** Ítem para domesticar (placeholder hasta que lo confirmes). */
    protected boolean isTameItem(ItemStack stack) {
        return stack.is(Items.BONE_MEAL);
    }

    private InteractionResult tryTame(Player player, ItemStack stack) {
        if (!isTameItem(stack)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (this.random.nextInt(PeekerConstants.TAME_CHANCE_ONE_IN) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.level().broadcastEntityEvent(this, (byte) 7); // corazones
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6); // humo
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult equipSaddle(Player player, ItemStack stack) {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        setSaddled(true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult unequipSaddle() {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        setSaddled(false);
        this.spawnAtLocation(Items.SADDLE);
        this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult mount(Player player) {
        if (!this.level().isClientSide) {
            this.navigation.stop();
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ------------------------------------------------------------------
    // Movimiento con jinete
    // ------------------------------------------------------------------

    @Override
    public boolean isPushedByFluid() {
        return !this.isVehicle();
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() + PeekerConstants.SADDLE_RIDER_Y_OFFSET;
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity rider = this.getControllingPassenger();

        if (this.isAlive() && this.isVehicle() && this.isSaddled() && rider != null) {
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(rider.getXRot() * PeekerConstants.SADDLED_RIDER_PITCH_FACTOR);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new Vec3(0.0D, 0.0D, rider.zza));
        } else {
            super.travel(travelVector);
        }
    }

    /**
     * Al desmontar: los goals dejan un camino pendiente que travel() ignoraba con
     * jinete y que se ejecutaría al bajarse. Cortamos navegación, velocidad
     * horizontal y animación de caminar para que quede quieto al instante.
     */
    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.navigation.stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        this.setSpeed(0.0F);
        this.walkAnimation.update(0.0F, 1.0F); // factor 1.0 = aplica 0 sin suavizado
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return this.isSaddled() && passenger instanceof LivingEntity livingEntity ? livingEntity : null;
    }
    // ------------------------------------------------------------------
    // Baja vida, muerte y explosión
    // ------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        tickLowHealthEffects();
    }

    private void tickLowHealthEffects() {
        if (this.getHealth() > PeekerConstants.LOW_HEALTH_THRESHOLD) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        RandomSource random = serverLevel.getRandom();
        if (random.nextInt(PeekerConstants.LOW_HEALTH_CHANCE_ONE_IN) != 0) return;

        int count = PeekerConstants.LOW_HEALTH_PARTICLE_MIN + random.nextInt(PeekerConstants.LOW_HEALTH_PARTICLE_EXTRA);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 3.0D, this.getZ(), count, 0.5D, 0.5D, 0.5D, 0.02D);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;

        // Tick 20: 25% de soltar el fetus
        if (this.deathTime == 20 && this.level() instanceof ServerLevel serverLevel) {
            if (this.random.nextInt(4) == 0) {
                ItemEntity fetus = new ItemEntity(serverLevel,
                        this.getX(), this.getY(), this.getZ(),
                        new ItemStack(Moditems.PEEKER_FETUS.get()));
                fetus.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(fetus);
            }
        }

        // Tick 25: explosión
        if (this.deathTime == PeekerConstants.DEATH_ANIMATION_TICKS) {
            if (this.level() instanceof ServerLevel serverLevel) {
                explodeEffects(serverLevel);
            }
            this.remove(Entity.RemovalReason.KILLED);
            SoundEvent explode = modSound(PeekerConstants.SOUND_EXPLODE);
            if (explode != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), explode, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    private void explodeEffects(ServerLevel level) {
        RandomSource random = level.getRandom();

        int explosionCount = PeekerConstants.EXPLOSION_PARTICLE_MIN + random.nextInt(PeekerConstants.EXPLOSION_PARTICLE_EXTRA);
        int critCount = PeekerConstants.CRIT_PARTICLE_MIN + random.nextInt(PeekerConstants.CRIT_PARTICLE_EXTRA);
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), explosionCount, 1.0D, 1.0D, 1.0D, 0.0D);
        level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(), critCount, 1.0D, 1.0D, 1.0D, 0.1D);

        AABB damageBox = this.getBoundingBox().inflate(PeekerConstants.EXPLOSION_DAMAGE_RADIUS);
        List<Entity> nearby = level.getEntities(this, damageBox, e -> e.isAlive() && !(e instanceof ItemEntity));
        for (Entity entity : nearby) {
            float damage = PeekerConstants.EXPLOSION_DAMAGE_MIN
                    + random.nextFloat() * (PeekerConstants.EXPLOSION_DAMAGE_MAX - PeekerConstants.EXPLOSION_DAMAGE_MIN);
            entity.hurt(this.damageSources().explosion(null, null), damage);
        }
        growNearbyGrass(level, random);

        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    private void growNearbyGrass(ServerLevel level, RandomSource random) {
        BlockPos center = this.blockPosition();
        int attempts = 10 + random.nextInt(6);

        for (int i = 0; i < attempts; i++) {
            BlockPos pos = center.offset(random.nextInt(3) - 1, -1, random.nextInt(3) - 1);
            BlockState state = level.getBlockState(pos);

            // El fetus es BonemealableBlock, pero la explosión no debe hacerlo eclosionar
            if (state.getBlock() instanceof PeekerFetusBlock) continue;

            if (state.getBlock() instanceof BonemealableBlock bonemealable
                    && bonemealable.isValidBonemealTarget(level, pos, state, level.isClientSide())) {
                bonemealable.performBonemeal(level, random, pos, state);
            }
        }
    }

    // ------------------------------------------------------------------
    // Bebé -> adulto y cría
    // ------------------------------------------------------------------

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions adult = super.getDimensions(pose).scale(PeekerConstants.BASE_ENTITY_SCALE);
        return this.isBaby() ? adult.scale(0.95F) : adult;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageable) {
        PeekerEntity baby = ModEntities.PEEKER.get().create(serverLevel);
        if (baby != null) {
            baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()), MobSpawnType.BREEDING, null, null);
        }
        return baby;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false; // pendiente: ítem de cría
    }

    // ------------------------------------------------------------------
    // GeckoLib
    // ------------------------------------------------------------------

    private PlayState movementPredicate(AnimationState<PeekerEntity> event) {
        if (this.isDeadOrDying()) {
            return event.setAndContinue(
                    RawAnimation.begin().thenPlay(PeekerConstants.ANIM_EXPLODE)
            );
        }

        Vec3 movement = this.getDeltaMovement();
        boolean moving = movement.horizontalDistanceSqr() > 1.0E-6D;

        if (moving) {
            return event.setAndContinue(
                    RawAnimation.begin().thenLoop(
                            this.isBaby() ? "walk_baby" : PeekerConstants.ANIM_WALK
                    )
            );
        }

        return event.setAndContinue(
                RawAnimation.begin().thenLoop(PeekerConstants.ANIM_IDLE)
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 0, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive() && !this.isRemoved();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
}
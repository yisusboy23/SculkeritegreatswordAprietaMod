package net.tucas.sculkeritegreatsword.entity.custom;

import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.tucas.sculkeritegreatsword.init.ModSounds;
import net.tucas.sculkeritegreatsword.item.Moditems;
import net.tucas.sculkeritegreatsword.init.ModMobEffects;
import net.tucas.sculkeritegreatsword.potion.MandrakeSongEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;

public class MandrakeEntity extends PathfinderMob implements GeoEntity {
    public int cooldown = 0;

    // Cuántos ticks lleva quieta sin llorar. Al pasar SLEEP_THRESHOLD, se duerme.
    private int idleTicks = 0;
    private static final int SLEEP_THRESHOLD = 400; // 20 segundos sin llorar

    public static final EntityDataAccessor<Boolean> CRYING =
            SynchedEntityData.defineId(MandrakeEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> ASLEEP =
            SynchedEntityData.defineId(MandrakeEntity.class, EntityDataSerializers.BOOLEAN);

    public boolean isCrying() {
        return this.entityData.get(CRYING);
    }

    public boolean isAsleep() {
        return this.entityData.get(ASLEEP);
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable) this);

    public MandrakeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        SingletonGeoAnimatable.registerSyncedAnimatable((GeoAnimatable) this);
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0F, 0.75D, 1.28D));
        this.goalSelector.addGoal(4, new MandrakeStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.4F;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Si está dormida, detener cualquier movimiento o navegación
        if (isAsleep()) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        }

        AABB aabb = new AABB(this.blockPosition()).inflate(3.0D);
        List<Player> playerList = this.level().getEntitiesOfClass(Player.class, aabb);
        List<LivingEntity> mobList = this.level().getEntitiesOfClass(
                LivingEntity.class,
                aabb,
                e -> !(e instanceof Player) && !(e instanceof MandrakeEntity)
        );

        if (!playerList.isEmpty() || !mobList.isEmpty()) {
            // Hay alguien cerca: llora, se despierta y reinicia el contador
            this.entityData.set(CRYING, true);
            this.entityData.set(ASLEEP, false);
            this.idleTicks = 0;

            for (Player player : playerList) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 101, 0, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 101, 1, true, true));
                player.addEffect(new MobEffectInstance(
                        ModMobEffects.MANDRAKE_SONG.get(),
                        MandrakeSongEffect.PLAYER_DURATION_TICKS,
                        0,
                        false, // ambient
                        true,  // showParticles
                        true   // showIcon
                ));
            }

            for (LivingEntity mob : mobList) {
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 101, 0, true, true));
                mob.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 101, 1, true, true));
                mob.addEffect(new MobEffectInstance(
                        ModMobEffects.MANDRAKE_SONG.get(),
                        MandrakeSongEffect.PLAYER_DURATION_TICKS,
                        0,
                        false,
                        true,
                        true
                ));
            }
            this.cooldown = 0;
        } else {
            // Nadie cerca
            if (this.cooldown < 60 && this.entityData.get(CRYING)) {
                this.cooldown++;
            }

            if (this.cooldown > 59) {
                this.entityData.set(CRYING, false);
            }

            // Solo cuenta el tiempo de sueño cuando ya no está llorando
            if (!this.entityData.get(CRYING)) {
                this.idleTicks++;

                if (this.idleTicks >= SLEEP_THRESHOLD) {
                    this.entityData.set(ASLEEP, true);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CRYING, false);
        this.entityData.define(ASLEEP, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("crying", this.entityData.get(CRYING));
        pCompound.putBoolean("asleep", this.entityData.get(ASLEEP));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.entityData.set(CRYING, pCompound.getBoolean("crying"));
        this.entityData.set(ASLEEP, pCompound.getBoolean("asleep"));
    }

    @Nullable
    public ItemStack getPickedResult() {
        return ((Item) Moditems.MANDRAKE_ROOT.get()).getDefaultInstance();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "move_and_idle", 0, this::movePredicate));
        controllerRegistrar.add(new AnimationController<>(this, "cry", 0, this::predicate)
                .setParticleKeyframeHandler(state -> {
                    if (state.getKeyframeData().getEffect().matches("tears")) {
                        this.level().addParticle(
                                (ParticleOptions) new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WATER.defaultBlockState()),
                                this.getX(), this.getY() + 0.4D, this.getZ(), 0.0D, 0.0D, 0.0D);
                    }
                }).setSoundKeyframeHandler(state -> {
                    MandrakeEntity mandrakeEntity = (MandrakeEntity) state.getAnimatable();
                    float roll = this.level().getRandom().nextFloat();
                    SoundEvent sound;
                    if (roll < 0.2D) sound = (SoundEvent) ModSounds.MANDRAKE_CRY_1.get();
                    else if (roll < 0.4D) sound = (SoundEvent) ModSounds.MANDRAKE_CRY_2.get();
                    else if (roll < 0.6D) sound = (SoundEvent) ModSounds.MANDRAKE_CRY_3.get();
                    else if (roll < 0.8D) sound = (SoundEvent) ModSounds.MANDRAKE_CRY_4.get();
                    else sound = (SoundEvent) ModSounds.MANDRAKE_CRY_5.get();
                    mandrakeEntity.level().playLocalSound(
                            mandrakeEntity.getX(), mandrakeEntity.getY(), mandrakeEntity.getZ(),
                            sound, mandrakeEntity.getSoundSource(), 0.1F, 1.0F, false);
                }));
    }

    private <T extends GeoAnimatable> PlayState movePredicate(AnimationState<T> state) {
        if (isAsleep()) {
            state.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        if (state.isMoving()) {
            state.getController().setAnimation(RawAnimation.begin().then("walking", Animation.LoopType.LOOP));
        } else {
            state.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> state) {
        if (isCrying()) {
            state.getController().setAnimation(RawAnimation.begin().then("crying", Animation.LoopType.LOOP));
        } else {
            state.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
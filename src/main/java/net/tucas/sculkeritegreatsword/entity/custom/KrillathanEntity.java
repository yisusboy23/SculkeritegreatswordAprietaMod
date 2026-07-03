package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.tucas.sculkeritegreatsword.entity.ai.goal.KrillathanLeapGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.KrillathanRandomSwimGoal;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.minecraft.world.item.ItemStack;
import net.tucas.sculkeritegreatsword.item.Moditems;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class KrillathanEntity extends WaterAnimal implements GeoEntity, Bucketable {


    private static final EntityDataAccessor<Integer> LEAP_STATE =
            SynchedEntityData.defineId(KrillathanEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(KrillathanEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation SWIM         = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation IDLE_SWIM    = RawAnimation.begin().thenLoop("idle_swim");
    private static final RawAnimation IDLE         = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SPLASH_START = RawAnimation.begin().then("splash_start", software.bernie.geckolib.core.animation.Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SPLASH       = RawAnimation.begin().thenLoop("splash");
    private static final RawAnimation SWIM_RIDE = RawAnimation.begin().thenLoop("swim_raid");

    // ── Partes multipart ─────────────────────────────────────────
    public final KrillathanPart headPart;
    public final KrillathanPart bodyPart;
    public final KrillathanPart tailPart;
    public final KrillathanPart nosePart;
    private final KrillathanPart[] allParts;

    public KrillathanEntity(EntityType<? extends KrillathanEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);

// DESPUÉS
        this.nosePart = new KrillathanPart(this, 3.0F, 1.5F);
        this.headPart = new KrillathanPart(this, 5.5F, 4.2F);
        this.bodyPart = new KrillathanPart(this, 5.0F, 3.0F);
        this.tailPart = new KrillathanPart(this, 5.0F, 3.0F);
        this.allParts = new KrillathanPart[]{nosePart, headPart, bodyPart, tailPart};
    }

    // ── SynchedData ──────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LEAP_STATE, 0);
        this.entityData.define(SADDLED, false);
    }

    public int getLeapState()           { return this.entityData.get(LEAP_STATE); }
    public void setLeapState(int state) { this.entityData.set(LEAP_STATE, state); }

    public boolean isSaddled() {
        return this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean value) {
        this.entityData.set(SADDLED, value);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Saddle", this.isSaddled());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSaddled(tag.getBoolean("Saddle"));
    }

    // ── Multipart ────────────────────────────────────────────────
    @Override
    public boolean isMultipartEntity() { return true; }

    @Override
    public PartEntity<?>[] getParts() { return allParts; }

    // ── Tick ─────────────────────────────────────────────────────
    private int growUpTimer = 0;
    private static final int GROW_UP_TICKS = 36000; // cambia a 6000 cuando termines de testear

    public boolean isBabyEntity() {
        return this.getType() == ModEntities.KRILLATHAN_BABY.get();
    }

    @Override
    public void tick() {
        this.tickMultipart();
        super.tick();

        if (isBabyEntity()) {
            if (!this.level().isClientSide()) {
                growUpTimer++;
                if (growUpTimer >= GROW_UP_TICKS) {
                    var adult = ModEntities.KRILLATHAN.get().create(this.level());
                    if (adult != null) {
                        float healthPercent = this.getHealth() / this.getMaxHealth();
                        adult.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                        adult.setHealth(adult.getMaxHealth() * healthPercent);
                        this.level().addFreshEntity(adult);
                    }
                    this.discard();
                }
            }
        }
    }

    private void tickMultipart() {
        // Si es bebé, mueve las partes fuera del mundo para desactivar sus hitboxes
        if (isBabyEntity()) {
            for (KrillathanPart part : allParts) {
                part.setPos(0, -9999, 0);
            }
            return;
        }

        Vec3[] prev = new Vec3[allParts.length];
        for (int i = 0; i < allParts.length; i++) {
            prev[i] = new Vec3(allParts[i].getX(), allParts[i].getY(), allParts[i].getZ());
        }

        Vec3 center = this.position().add(0, this.getBbHeight() * 0.5F, 0);
        float spacing = 5.0F;
        float yaw = this.getYRot() * ((float) Math.PI / 180F);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));

        this.nosePart.setPosCenteredY(center.add(forward.scale(spacing + 4.5F)).add(0, 2.5, 0));
        this.headPart.setPosCenteredY(center.add(forward.scale(spacing)).add(0, 1.5, 0));
        this.bodyPart.setPosCenteredY(center.add(0, 1.0, 0));
        this.tailPart.setPosCenteredY(center.subtract(forward.scale(spacing)).add(0, 1.0, 0));

        for (int i = 0; i < allParts.length; i++) {
            allParts[i].xo    = prev[i].x;
            allParts[i].yo    = prev[i].y;
            allParts[i].zo    = prev[i].z;
            allParts[i].xOld  = prev[i].x;
            allParts[i].yOld  = prev[i].y;
            allParts[i].zOld  = prev[i].z;
        }
    }

    // ── Remove ───────────────────────────────────────────────────
    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        if (allParts != null) {
            for (KrillathanPart part : allParts) {
                part.remove(RemovalReason.KILLED);
            }
        }
    }


    // ── Navegación en agua ───────────────────────────────────────
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    // ── Atributos ────────────────────────────────────────────────
    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH,     150.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE,  4.0);
    }

    // ── Goals ────────────────────────────────────────────────────
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(1, new KrillathanLeapGoal(this));
        this.goalSelector.addGoal(2, new KrillathanRandomSwimGoal(this, 1.0D, 20, 30, 4));
        // SOLO mira si está en agua
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && KrillathanEntity.this.isInWaterOrBubble();
            }
        });

        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && KrillathanEntity.this.isInWaterOrBubble();
            }
        });
    }

    // ── Sonidos ──────────────────────────────────────────────────
    @Nullable @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.DOLPHIN_AMBIENT_WATER; }
    @Nullable @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.DOLPHIN_HURT; }
    @Nullable @Override
    protected SoundEvent getDeathSound() { return SoundEvents.DOLPHIN_DEATH; }

    // ── Escala bebé ──────────────────────────────────────────────
    @Override
    public float getScale() {
        return isBabyEntity() ? 0.45F : 1.0F;
    }
    // ── Spawn con posibilidad de bebé ────────────────────────────
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable net.minecraft.nbt.CompoundTag dataTag) {
        // Ya no usamos setBaby() — el bebé es una entidad distinta
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    // ── Movimiento en agua ───────────────────────────────────────
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive() && this.isVehicle()) {
            if (this.getFirstPassenger() instanceof net.minecraft.world.entity.player.Player rider) {

                // Siempre sincronizar rotación con el rider
                this.setYRot(rider.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(rider.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;

                boolean hasStick = rider.getMainHandItem().is(Moditems.KELP_ON_A_STICK.get());

                if (hasStick) {
                    // NO usar setSpeed ni super.travel — ambos pasan por la fricción del agua
                    float speed = 0.45F; // Ajusta este valor (prueba 0.3 a 0.6)
                    float forward = rider.zza;
                    float strafe  = rider.xxa;
                    float pitch   = rider.getXRot();
                    // Input vertical basado en donde mira el jugador
                    double yInput = -Math.sin(pitch * (Math.PI / 180F)) * speed;
                    yInput = Math.max(-0.4, Math.min(0.4, yInput));
                    // Convertir input local → mundo
                    Vec3 lookVec  = this.getLookAngle();
                    Vec3 rightVec = new Vec3(lookVec.z, 0, -lookVec.x).normalize();
                    double targetX = (strafe * rightVec.x + forward * lookVec.x) * speed;
                    double targetZ = (strafe * rightVec.z + forward * lookVec.z) * speed;
                    double targetY = yInput;
                    // Interpolación suave (evita arranques bruscos)
                    Vec3 current = this.getDeltaMovement();
                    this.setDeltaMovement(
                            current.x + (targetX - current.x) * 0.4,
                            current.y + (targetY - current.y) * 0.4,
                            current.z + (targetZ - current.z) * 0.4
                    );
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.hasImpulse = true;
                    return;
                }
                // Sin stick → IA deambula, pero sin input del rider
                super.travel(new Vec3(0, travelVector.y, 0));
                return;
            }
        }

        // Sin rider → comportamiento normal en agua
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
        }
    }

    // ── GeckoLib – animaciones ───────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            int leapState = this.getLeapState();

            if (leapState == 2) return state.setAndContinue(SPLASH);

            // Rider con kelp on a stick
            if (this.isVehicle() && this.canBeControlledByRider()) {
                boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
                if (isMoving) return state.setAndContinue(SWIM_RIDE);
                return state.setAndContinue(IDLE_SWIM);
            }

            if (this.isInWater() || this.isInWaterOrBubble()) {
                if (state.isMoving()) return state.setAndContinue(SWIM);
                return state.setAndContinue(IDLE_SWIM);
            }

            return state.setAndContinue(IDLE);
        }));
    }
    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.allParts.length; i++) {
            this.allParts[i].setId(id + i + 1);
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide()) {
            net.minecraft.server.level.ServerLevel server =
                    (net.minecraft.server.level.ServerLevel) this.level();
            for (KrillathanPart part : allParts) {
                server.addFreshEntity(part);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(12, 6, 12);
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    // ── CUBETA ───────────────────────────────────
    private boolean fromBucket = false;

    @Override
    public net.minecraft.world.InteractionResult mobInteract(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {

        // Bebé → solo cubeta
        if (isBabyEntity()) {
            return Bucketable.bucketMobPickup(player, hand, this)
                    .orElse(net.minecraft.world.InteractionResult.PASS);
        }


        ItemStack item = player.getItemInHand(hand);
        // Curación con kelp
        if (item.is(net.minecraft.world.item.Items.KELP)) {
            if (this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    this.heal(4.0F); // 2 corazones
                    if (!player.getAbilities().instabuild) {
                        item.shrink(1);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }

        // Curación con bloque de kelp seco
        if (item.is(net.minecraft.world.item.Items.DRIED_KELP_BLOCK)) {
            if (this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    this.heal(20.0F);
                    if (!player.getAbilities().instabuild) {
                        item.shrink(1);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }

        // Shift + click → quitar saddle
        if (player.isShiftKeyDown()) {
            if (this.isSaddled() && !this.level().isClientSide()) {
                this.setSaddled(false);
                this.spawnAtLocation(net.minecraft.world.item.Items.SADDLE);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Click con saddle en mano → equipar
        if (item.is(net.minecraft.world.item.Items.SADDLE) && !this.isSaddled()) {
            if (!this.level().isClientSide()) {
                this.setSaddled(true);
                item.shrink(1);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Click normal con saddle equipada → montar
        if (this.isSaddled()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean fromBucket() { return this.fromBucket; }

    @Override
    public void setFromBucket(boolean fromBucket) { this.fromBucket = fromBucket; }

    @Override
    public void saveToBucketTag(ItemStack bucket) {
        net.minecraft.nbt.CompoundTag tag = bucket.getOrCreateTag();
        tag.putBoolean("fromBucket", true);
    }

    @Override
    public void loadFromBucketTag(net.minecraft.nbt.CompoundTag tag) {
        this.fromBucket = tag.getBoolean("fromBucket");
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Moditems.BABY_KRILLATHAN_BUCKET.get());
    }

    @Override
    public net.minecraft.sounds.SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_FISH;
    }
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }


    public boolean canBeControlledByRider() {
        if (this.getFirstPassenger() instanceof net.minecraft.world.entity.player.Player player) {
            return player.getMainHandItem().is(Moditems.KELP_ON_A_STICK.get());
        }
        return false;
    }
    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {

            float yaw = this.getYRot() * ((float)Math.PI / 180F);

            double forwardOffset = 6.7;
            double heightOffset = 3.95;

            double x = this.getX() - Math.sin(yaw) * forwardOffset;
            double z = this.getZ() + Math.cos(yaw) * forwardOffset;
            double y = this.getY() + heightOffset;

            moveFunction.accept(passenger, x, y, z);
        }
    }

}
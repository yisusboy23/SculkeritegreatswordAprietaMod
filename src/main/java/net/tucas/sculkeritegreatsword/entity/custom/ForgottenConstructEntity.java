package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.ai.goal.*;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ForgottenConstructEntity extends Monster implements GeoEntity {

    /** Prefijo de las animaciones en el .animation.json (ej. "animation.forgotten_construct."). */
    public static final String ANIM_PREFIX = "";
    /** Marca NBT que llevan las bolas de fuego disparadas por el jefe. */
    public static final String FIREBALL_TAG = "ForgottenConstructFireball";

    public static final double MELEE_RANGE = 4.0D;
    public static final double RANGED_MAX_RANGE = 12.0D;
    public static final int FAR_TICKS_FOR_PULL = 60;          // 3 s
    public static final float PHASE2_HEALTH_RATIO = 0.30F;
    private static final double PHASE2_SPEED = 1.35D;

    // Locators del .geo.json (unidades de modelo)
    public static final Vec3 NUCLEO_OFFSET = new Vec3(0.5D, 14.75D, -6.5D);
    public static final Vec3 CANNON_OFFSET = new Vec3(9.5D, 33.6D, -32.2D);
    /** Si la bola o el rayo salen del lado contrario, cambia a -1. */
    private static final double X_SIGN = 1.0D;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(ForgottenConstructEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_AWAKE =
            SynchedEntityData.defineId(ForgottenConstructEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PHASE2 =
            SynchedEntityData.defineId(ForgottenConstructEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BEAM_YAW =
            SynchedEntityData.defineId(ForgottenConstructEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_PITCH =
            SynchedEntityData.defineId(ForgottenConstructEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_LEN =
            SynchedEntityData.defineId(ForgottenConstructEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int meleeCooldown, rangedCooldown, pullCooldown, farTicks;

    public ForgottenConstructEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // ------------------------------------------------------------------ GOALS
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BossSleepGoal(this));
        this.goalSelector.addGoal(1, new BossWakeUpGoal(this));
        this.goalSelector.addGoal(2, new BossDazedGoal(this));
        this.goalSelector.addGoal(3, new BossPullPlayersGoal(this));
        this.goalSelector.addGoal(4, new BossMeleeComboGoal(this));
        this.goalSelector.addGoal(5, new BossRangedAttackGoal(this));
        this.goalSelector.addGoal(6, new BossLookAtTargetGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    // ------------------------------------------------------------------ DATA
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, BossState.SLEEP.ordinal());
        this.entityData.define(DATA_AWAKE, false);
        this.entityData.define(DATA_PHASE2, false);
        this.entityData.define(BEAM_YAW, 0.0F);
        this.entityData.define(BEAM_PITCH, 0.0F);
        this.entityData.define(BEAM_LEN, 0.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Awake", isAwake());
        tag.putBoolean("Phase2", isPhase2());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAwake(tag.getBoolean("Awake"));
        setPhase2(tag.getBoolean("Phase2"));
        setBossState(isAwake() ? BossState.IDLE : BossState.SLEEP);
    }

    public BossState getBossState() { return BossState.byId(this.entityData.get(DATA_STATE)); }
    public void setBossState(BossState s) { this.entityData.set(DATA_STATE, s.ordinal()); }
    public boolean isAwake() { return this.entityData.get(DATA_AWAKE); }
    public void setAwake(boolean v) { this.entityData.set(DATA_AWAKE, v); }
    public boolean isPhase2() { return this.entityData.get(DATA_PHASE2); }
    public void setPhase2(boolean v) { this.entityData.set(DATA_PHASE2, v); }

    // ------------------------------------------------------------------ RAYO Y LOCATORS
    public void setBeam(float yaw, float pitch, float len) {
        entityData.set(BEAM_YAW, yaw);
        entityData.set(BEAM_PITCH, pitch);
        entityData.set(BEAM_LEN, len);
    }
    public void clearBeam() { entityData.set(BEAM_LEN, 0.0F); }
    public float getBeamYaw() { return entityData.get(BEAM_YAW); }
    public float getBeamPitch() { return entityData.get(BEAM_PITCH); }
    public float getBeamLen() { return entityData.get(BEAM_LEN); }
    public boolean isBeamActive() { return getBossState() == BossState.BEAM && getBeamLen() > 0.0F; }

    /** Offset de un locator relativo a la posición del jefe, en ejes de mundo. */
    public Vec3 locatorOffset(Vec3 o, float bodyYawDeg) {
        float yaw = bodyYawDeg * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
        Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));

        return fwd.scale(-o.z / 16.0D)
                .add(right.scale(X_SIGN * o.x / 16.0D))
                .add(0, o.y / 16.0D, 0);
    }

    /** Posición en el mundo de un locator. */
    public Vec3 locatorPos(Vec3 o) { return position().add(locatorOffset(o, yHeadRot)); }

    /** Evita que el rayo desaparezca si el jefe sale de pantalla. */
    @Override
    public AABB getBoundingBoxForCulling() { return super.getBoundingBoxForCulling().inflate(22.0D); }

    // Capas emisivas (las lee el renderer en el cliente)
    public boolean isGlowActive() { return isAwake() && getBossState() != BossState.DAZED; }
    public boolean isOverpowerActive() {
        return isGlowActive() && (isPhase2() || getBossState().usesOverpower());
    }

    // Fase 2: ataques más rápidos
    public double getAttackSpeedMultiplier() { return isPhase2() ? PHASE2_SPEED : 1.0D; }
    public int scaledTicks(int base) { return Math.max(1, Math.round(base / (float) getAttackSpeedMultiplier())); }

    public int getMeleeCooldown() { return meleeCooldown; }
    public void setMeleeCooldown(int t) { meleeCooldown = t; }
    public int getRangedCooldown() { return rangedCooldown; }
    public void setRangedCooldown(int t) { rangedCooldown = t; }
    public int getPullCooldown() { return pullCooldown; }
    public void setPullCooldown(int t) { pullCooldown = t; }
    public int getFarTicks() { return farTicks; }
    public void resetFarTicks() { farTicks = 0; }

    // ------------------------------------------------------------------ TICK
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (meleeCooldown > 0) meleeCooldown--;
        if (rangedCooldown > 0) rangedCooldown--;
        if (pullCooldown > 0) pullCooldown--;

        // Contador anti-camping
        LivingEntity t = getTarget();
        if (isAwake() && t != null && distanceToSqr(t) > RANGED_MAX_RANGE * RANGED_MAX_RANGE) farTicks++;
        else farTicks = 0;

        // Entrada a Fase 2 (solo entre ataques): rugido + ataques más rápidos
        if (isAwake() && !isPhase2() && getBossState() == BossState.IDLE
                && getHealth() < getMaxHealth() * PHASE2_HEALTH_RATIO) {
            setPhase2(true);
            setBossState(BossState.WAKING_UP);
        }
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (getBossState() == BossState.SLEEP) target = null;   // dormido no fija objetivo
        super.setTarget(target);
    }

    // ------------------------------------------------------------------ DAÑO / INMUNIDADES
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return super.hurt(source, amount);

        // Despertar al recibir un golpe
        if (getBossState() == BossState.SLEEP && source.getEntity() != null) {
            setBossState(BossState.WAKING_UP);
        }

        Entity direct = source.getDirectEntity();
        boolean projectile = direct instanceof Projectile || source.is(DamageTypeTags.IS_PROJECTILE);

        if (projectile && getBossState() != BossState.DAZED) {
            if (isReturnedFireball(direct)) {
                enterDazed();                       // la bola devuelta lo aturde
                return super.hurt(source, amount);
            }
            return false;                           // inmune a cualquier otro proyectil
        }
        return super.hurt(source, amount);
    }

    /** Bola de fuego del jefe que un jugador (u otra entidad) ha devuelto. */
    private boolean isReturnedFireball(Entity e) {
        return e instanceof Fireball f
                && f.getPersistentData().getBoolean(FIREBALL_TAG)
                && f.getOwner() instanceof LivingEntity owner
                && owner != this;
    }

    public void enterDazed() {
        clearBeam();
        setBossState(BossState.DAZED);
        playSound(SoundEvents.ANVIL_LAND, 2.0F, 0.6F);
    }

    // ------------------------------------------------------------------ HELPERS DE COMBATE
    public List<LivingEntity> getVictimsAround(double radius) {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 2.0D, radius),
                e -> e != this && e.isAlive() && !(e instanceof ForgottenConstructEntity)
                        && (!(e instanceof Player p) || (!p.isCreative() && !p.isSpectator()))
                        && distanceToSqr(e) <= radius * radius);
    }

    public void hurtAndPush(LivingEntity victim, float damage, double horizontal, double vertical) {
        victim.hurt(level().damageSources().mobAttack(this), damage);
        Vec3 away = new Vec3(victim.getX() - getX(), 0, victim.getZ() - getZ());
        away = away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize();
        victim.setDeltaMovement(victim.getDeltaMovement().add(away.x * horizontal, vertical, away.z * horizontal));
        victim.hurtMarked = true;   // sincroniza el impulso con el cliente del jugador
    }

    // ------------------------------------------------------------------ ESTATUA INMÓVIL
    @Override public boolean isPushable() { return false; }
    @Override public void push(Entity entity) { }
    @Override public void push(double x, double y, double z) { }
    @Override protected void doPush(Entity entity) { }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public void knockback(double strength, double x, double z) { }
    @Override public boolean fireImmune() { return true; }
    @Override public boolean causeFallDamage(float dist, float mult, DamageSource src) { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }

    /** Solo permite el movimiento vertical (gravedad); jamás se desplaza en X/Z. */
    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(0, movement.y, 0));
    }

    /** La base del cuerpo no rota nunca; solo la cabeza sigue al objetivo. */
    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override public void clientTick() { }
        };
    }

    @Override public int getMaxHeadYRot() { return 180; }

    // ------------------------------------------------------------------ GECKOLIB
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<ForgottenConstructEntity> state) {
        BossState s = getBossState();
        state.getController().setAnimationSpeed(isPhase2() && s.isAttack() ? PHASE2_SPEED : 1.0D);
        return state.setAndContinue(s.getAnimation());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
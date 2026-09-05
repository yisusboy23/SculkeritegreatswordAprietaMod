package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ResonarchDefendAndRetreatGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ResonarchHoverWanderGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ResonarchPsychicAttackGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ResonarchLookAtPlayerGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class ResonarchEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Boolean> SHIELD_ACTIVE =
            SynchedEntityData.defineId(ResonarchEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COMBAT_STATE =
            SynchedEntityData.defineId(ResonarchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> REALLY_MOVING =
            SynchedEntityData.defineId(ResonarchEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int STATE_IDLE = 0;
    public static final int STATE_DEFEND = 1;
    public static final int STATE_DEFEND_IDLE = 2;
    public static final int STATE_REFLECT = 3;
    public static final int STATE_PSYCHIC_ATTACK = 4;

    private static final int DEFEND_ANIM_TICKS = 20;
    private static final int SHIELD_TIMEOUT_TICKS = 50;
    private static final int REFLECT_DURATION_TICKS = 30;
    private static final int LAUNCH_DELAY_TICKS = 12;
    private static final int PSYCHIC_COOLDOWN_TICKS = 100;
    private static final int PSYCHIC_ATTACK_DURATION_TICKS = REFLECT_DURATION_TICKS;

    private static final double MOVING_THRESHOLD_SQ = 0.0009D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final class FrozenProjectile {
        final Projectile projectile;
        final Vec3 anchor;
        FrozenProjectile(Projectile projectile, Vec3 anchor) {
            this.projectile = projectile;
            this.anchor = anchor;
        }
    }

    private final List<FrozenProjectile> frozenProjectiles = new ArrayList<>();

    private int shieldNoHitTicks = 0;
    private int reflectTicks = 0;
    private boolean reflectLaunched = false;
    private int psychicCooldown = 0;
    private int psychicAttackTicks = 0;

    public ResonarchEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new ResonarchMoveControl(this);
        this.setNoGravity(true);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D);
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(
                1,
                new ResonarchDefendAndRetreatGoal(this)
        );

        this.goalSelector.addGoal(
                2,
                new ResonarchPsychicAttackGoal(this)
        );

        this.goalSelector.addGoal(
                3,
                new ResonarchLookAtPlayerGoal(this)
        );

        this.goalSelector.addGoal(
                5,
                new ResonarchHoverWanderGoal(this)
        );

        this.goalSelector.addGoal(
                8,
                new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(
                1,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHIELD_ACTIVE, false);
        this.entityData.define(COMBAT_STATE, STATE_IDLE);
        this.entityData.define(REALLY_MOVING, false);
    }

    public boolean isShieldActive() {
        return this.entityData.get(SHIELD_ACTIVE);
    }

    public int getCombatState() {
        return this.entityData.get(COMBAT_STATE);
    }

    public void setCombatState(int state) {
        this.entityData.set(COMBAT_STATE, state);
        this.entityData.set(SHIELD_ACTIVE, state == STATE_DEFEND || state == STATE_DEFEND_IDLE || state == STATE_REFLECT);
    }

    public boolean isExclusiveActionActive() {
        int state = getCombatState();
        return state == STATE_REFLECT || state == STATE_PSYCHIC_ATTACK;
    }

    public boolean hasFrozenProjectiles() {
        return !frozenProjectiles.isEmpty();
    }

    public boolean isReadyForPsychicAttack() {
        return getCombatState() == STATE_IDLE && psychicCooldown <= 0 && frozenProjectiles.isEmpty();
    }

    public void startPsychicAttackState() {
        setCombatState(STATE_PSYCHIC_ATTACK);
        this.psychicAttackTicks = PSYCHIC_ATTACK_DURATION_TICKS;
        this.psychicCooldown = PSYCHIC_COOLDOWN_TICKS;
    }

    public boolean isReallyMoving() {
        return this.entityData.get(REALLY_MOVING);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        Vec3 horizontal = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        boolean nowReallyMoving = horizontal.lengthSqr() > MOVING_THRESHOLD_SQ;
        if (nowReallyMoving != this.entityData.get(REALLY_MOVING)) {
            this.entityData.set(REALLY_MOVING, nowReallyMoving);
        }

        if (psychicCooldown > 0) psychicCooldown--;

        int state = getCombatState();

        if (isShieldActive() || !frozenProjectiles.isEmpty()) {
            if (state == STATE_DEFEND || state == STATE_DEFEND_IDLE || state == STATE_IDLE) {
                shieldNoHitTicks++;
                if (shieldNoHitTicks >= SHIELD_TIMEOUT_TICKS) {
                    startReflect();
                } else if (state == STATE_DEFEND && shieldNoHitTicks >= DEFEND_ANIM_TICKS) {
                    setCombatState(STATE_DEFEND_IDLE);
                }
            }
        }

        if (state == STATE_REFLECT) {
            reflectTicks--;
            if (!reflectLaunched && reflectTicks <= REFLECT_DURATION_TICKS - LAUNCH_DELAY_TICKS) {
                launchFrozenProjectiles();
                reflectLaunched = true;
            }
            if (reflectTicks <= 0) {
                endShield();
            }
        } else if (state == STATE_PSYCHIC_ATTACK) {
            psychicAttackTicks--;
            if (psychicAttackTicks <= 0) {
                if (!frozenProjectiles.isEmpty()) {
                    setCombatState(STATE_DEFEND_IDLE);
                    shieldNoHitTicks = DEFEND_ANIM_TICKS;
                } else {
                    setCombatState(STATE_IDLE);
                }
            }
        }

        pinFrozenProjectiles();
    }

    private void pinFrozenProjectiles() {
        if (frozenProjectiles.isEmpty()) return;
        frozenProjectiles.removeIf(fp -> fp.projectile == null || !fp.projectile.isAlive());

        boolean spawnParticles = this.level() instanceof ServerLevel && this.tickCount % 4 == 0;
        for (FrozenProjectile fp : frozenProjectiles) {
            Projectile projectile = fp.projectile;
            projectile.setDeltaMovement(Vec3.ZERO);
            projectile.setPos(fp.anchor.x, fp.anchor.y, fp.anchor.z);
            projectile.hurtMarked = false;
            projectile.setNoGravity(true);

            if (spawnParticles) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.PORTAL,
                        fp.anchor.x, fp.anchor.y, fp.anchor.z,
                        2, 0.15D, 0.15D, 0.15D, 0.01D);
            }
        }
    }

    private void startReflect() {
        setCombatState(STATE_REFLECT);
        reflectTicks = REFLECT_DURATION_TICKS;
        reflectLaunched = false;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                    SoundSource.HOSTILE, 1.0F, 0.8F);
        }
    }

    private void endShield() {
        setCombatState(STATE_IDLE);
        shieldNoHitTicks = 0;
        releaseFrozenProjectiles();
    }

    private void releaseFrozenProjectiles() {
        for (FrozenProjectile fp : frozenProjectiles) {
            if (fp.projectile != null && fp.projectile.isAlive()) {
                fp.projectile.setNoGravity(false);
            }
        }
        frozenProjectiles.clear();
    }

    private void launchFrozenProjectiles() {
        Player fallbackShooter = null;
        for (FrozenProjectile fp : frozenProjectiles) {
            Projectile projectile = fp.projectile;
            if (projectile == null || !projectile.isAlive()) continue;

            Entity shooter = projectile.getOwner();
            if (shooter == null) {
                if (fallbackShooter == null) {
                    fallbackShooter = level().getNearestPlayer(this, 32.0D);
                }
                shooter = fallbackShooter;
            }
            if (shooter == null) continue;

            projectile.setNoGravity(false);

            Vec3 aimPoint = shooter.position().add(0, shooter.getEyeHeight() * 0.5, 0);
            Vec3 dir = aimPoint.subtract(projectile.position()).normalize();

            double speed = 2.2D;
            projectile.setDeltaMovement(dir.scale(speed));
            projectile.hasImpulse = true;

            if (projectile instanceof AbstractArrow arrow) {
                arrow.setOwner(this);
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        projectile.getX(), projectile.getY(), projectile.getZ(),
                        6, 0.05D, 0.05D, 0.05D, 0.02D);
            }
        }
        frozenProjectiles.clear();
    }

    public void onProjectileImpact(Projectile projectile) {
        int state = getCombatState();
        if (state == STATE_REFLECT) return;

        Vec3 anchor = projectile.position();
        projectile.setDeltaMovement(Vec3.ZERO);
        projectile.setNoGravity(true);
        projectile.hurtMarked = false;

        boolean alreadyTracked = frozenProjectiles.stream().anyMatch(fp -> fp.projectile == projectile);
        if (!alreadyTracked) {
            frozenProjectiles.add(new FrozenProjectile(projectile, anchor));
        }

        shieldNoHitTicks = 0;

        if (state == STATE_PSYCHIC_ATTACK) return;

        if (state != STATE_DEFEND_IDLE) {
            setCombatState(STATE_DEFEND);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile projectile && isProjectileFrozen(projectile)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        releaseFrozenProjectiles();
        super.remove(reason);
    }

    // ---- GeckoLib ----
    // FIX #8: Eliminado triggerableAnim para controlar el 100% de la animación vía predicate
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bossController", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<ResonarchEntity> state) {
        switch (getCombatState()) {
            case STATE_DEFEND -> state.getController().setAnimation(RawAnimation.begin().thenPlay("defend"));
            case STATE_DEFEND_IDLE -> state.getController().setAnimation(RawAnimation.begin().thenLoop("defend_idle"));
            case STATE_REFLECT, STATE_PSYCHIC_ATTACK -> state.getController().setAnimation(RawAnimation.begin().thenPlay("reflect"));
            default -> {
                if (this.isReallyMoving()) {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("float"));
                } else {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                }
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean willCatchProjectile() {
        return getCombatState() != STATE_REFLECT;
    }

    public boolean isProjectileFrozen(Projectile projectile) {
        return frozenProjectiles.stream().anyMatch(fp -> fp.projectile == projectile);
    }
}
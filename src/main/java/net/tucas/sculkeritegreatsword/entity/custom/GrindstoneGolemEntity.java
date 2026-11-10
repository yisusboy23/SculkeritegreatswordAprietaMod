package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.ai.goal.GrindstoneGolemRollingAttackGoal;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.List;

public class GrindstoneGolemEntity extends BaseGolemEntity {

    private static final RawAnimation ROLLING_START = RawAnimation.begin().thenPlay("rolling_start");
    private static final RawAnimation ROLLING = RawAnimation.begin().thenLoop("rolling");
    private static final RawAnimation ROLLING_END = RawAnimation.begin().thenPlay("rolling_end");

    private static final EntityDataAccessor<Boolean> IS_ROLLING =
            SynchedEntityData.defineId(GrindstoneGolemEntity.class, EntityDataSerializers.BOOLEAN);

    // ROLLING (solo servidor)
    private static final int ROLLING_COOLDOWN = 120;
    private int rollingTicks = 0;
    private int rollingPhase = 0;
    private LivingEntity rollingTarget = null;
    private int rollingCooldown = 0;
    private int stuckTicks = 0;
    private Vec3 lastPos = Vec3.ZERO;

    public GrindstoneGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.5f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(50.0, 0.25, 4.0, 6.0, 48.0, 0.8);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ROLLING, false);
    }

    public boolean isRolling() {
        return this.entityData.get(IS_ROLLING);
    }

    private void setRolling(boolean rolling) {
        this.entityData.set(IS_ROLLING, rolling);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new GrindstoneGolemRollingAttackGoal(this, 16.0, 5.0));
    }

    // ---------- Melee (25 ticks, golpe cuando quedan 13) ----------
    @Override
    protected boolean hasMeleeAnimation() {
        return true;
    }

    @Override
    protected int getMeleeDuration() {
        return 25;
    }

    @Override
    protected int getMeleeHitTick() {
        return 13;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (this.isRolling()) {
            return false; // no golpea mientras rueda
        }
        return super.doHurtTarget(target);
    }

    // ---------- Sonidos ----------
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    // ---------- NBT (solo lo persistente: el cooldown) ----------
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("RollingCooldown", this.rollingCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.rollingCooldown = compound.getInt("RollingCooldown");
    }

    // ---------- Tick ----------
    @Override
    protected void serverTick() {
        if (this.rollingCooldown > 0) {
            this.rollingCooldown--;
        }

        if (this.rollingTicks > 0) {
            this.rollingTicks--;

            if (this.rollingPhase == 1) {
                if (this.rollingTicks <= 0) {
                    this.rollingPhase = 2;
                    this.rollingTicks = 60;
                    this.stuckTicks = 0;
                    this.lastPos = this.position();
                    this.triggerAnim(CONTROLLER_MAIN, "rolling_loop");
                }
            } else if (this.rollingPhase == 2) {
                this.tickRollingMovement();
            } else if (this.rollingPhase == 3) {
                if (this.rollingTicks == 13) {
                    this.executeRollingDamage();
                }
                if (this.rollingTicks <= 0) {
                    this.stopRolling();
                }
            }
        }
    }

    private void tickRollingMovement() {
        if (this.rollingTarget == null || !this.rollingTarget.isAlive()) {
            this.stopRolling();
            return;
        }

        // Detectar si está trabado
        this.stuckTicks++;
        if (this.stuckTicks >= 20) {
            double moved = this.position().distanceTo(this.lastPos);
            if (moved < 0.5) {
                this.stopRolling();
                return;
            }
            this.lastPos = this.position();
            this.stuckTicks = 0;
        }

        double distSq = this.distanceToSqr(this.rollingTarget);

        if (distSq <= 4.0) {
            this.rollingPhase = 3;
            this.rollingTicks = 25;
            this.triggerAnim(CONTROLLER_MAIN, "rolling_end");
            return;
        }

        if (distSq > 900.0) {
            this.stopRolling();
            return;
        }

        Vec3 direction = this.rollingTarget.position().subtract(this.position()).normalize();

        // Salto automático para subir bloques
        if (this.horizontalCollision && this.onGround()) {
            this.setDeltaMovement(direction.x * 0.6, 0.5, direction.z * 0.6);
        } else {
            this.setDeltaMovement(direction.x * 0.6, this.getDeltaMovement().y, direction.z * 0.6);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    3, 0.3, 0.1, 0.3, 0.02);
        }
    }

    private void executeRollingDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(1.5);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !(e instanceof GrindstoneGolemEntity) && !this.isAllyOrOwner(e));

        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().mobAttack(this), 12.0f);

            Vec3 dir = target.position().subtract(this.position()).normalize();
            target.setDeltaMovement(dir.x * 1.0, 0.4, dir.z * 1.0);
            target.hurtMarked = true;

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 255, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 255, false, false));
        }

        serverLevel.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.5, this.getZ(),
                20, 1.0, 0.3, 1.0, 0.1);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.IRON_GOLEM_ATTACK, this.getSoundSource(), 1.5f, 0.6f);
    }

    // ---------- Control del rolling (usado por GrindstoneGolemRollingAttackGoal) ----------
    public boolean canUseRolling() {
        return this.rollingCooldown <= 0 && !this.isRolling() && !this.isActionAnimating();
    }

    public void startRollingAttack(LivingEntity target) {
        this.rollingTarget = target;
        this.rollingPhase = 1;
        this.rollingTicks = 10;
        this.rollingCooldown = ROLLING_COOLDOWN;
        this.setRolling(true);
        this.stuckTicks = 0;
        this.lastPos = this.position();

        this.triggerAnim(CONTROLLER_MAIN, "rolling_start");

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.STONE_FALL, this.getSoundSource(), 1.5f, 0.8f);
    }

    public void stopRolling() {
        this.rollingPhase = 0;
        this.rollingTicks = 0;
        this.rollingTarget = null;
        this.setRolling(false);
        this.stuckTicks = 0;
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.1, 1.0, 0.1));
    }

    @Override
    protected boolean shouldFreezeMovement() {
        return this.rollingPhase == 1 || this.rollingPhase == 3 || this.isActionAnimating();
    }

    // ---------- Animación ----------
    @Override
    protected boolean isPlayingCustomAction() {
        return this.isRolling(); // dato sincronizado, funciona también en el cliente
    }

    @Override
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {
        controller.triggerableAnim("rolling_start", ROLLING_START);
        controller.triggerableAnim("rolling_loop", ROLLING);
        controller.triggerableAnim("rolling_end", ROLLING_END);
    }
}
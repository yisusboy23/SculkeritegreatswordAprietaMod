package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ResonarchPsychicAttackGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ResonarchEntity extends Monster implements GeoEntity {

    // ---- Estado sincronizado cliente/servidor ----
    private static final EntityDataAccessor<Boolean> SHIELD_ACTIVE =
            SynchedEntityData.defineId(ResonarchEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COMBAT_STATE =
            SynchedEntityData.defineId(ResonarchEntity.class, EntityDataSerializers.INT);

    public static final int STATE_IDLE = 0;
    public static final int STATE_DEFEND = 1;
    public static final int STATE_DEFEND_IDLE = 2;
    public static final int STATE_REFLECT = 3;

    private static final int DEFEND_ANIM_TICKS = 20;       // 1.0s
    private static final int SHIELD_TIMEOUT_TICKS = 50;    // 2.5s sin nuevos impactos -> reflect
    private static final int REFLECT_DURATION_TICKS = 30;  // 1.5s, igual a la animación reflect
    private static final int LAUNCH_DELAY_TICKS = 12;      // "carga" de telekinesis antes de lanzar
    private static final int PSYCHIC_COOLDOWN_TICKS = 100; // 5.0s

    // Umbral de velocidad horizontal^2 para considerar que realmente se está desplazando.
    // isMoving() de GeckoLib no sirve aquí porque el mob flota y avanza en pasos muy pequeños.
    private static final double MOVING_THRESHOLD_SQ = 0.0009D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Proyectil congelado + punto exacto donde quedó "clavado" en el escudo. */
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
    private boolean reallyMoving = false;

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
        this.goalSelector.addGoal(1, new ResonarchKeepDistanceGoal(this, 6.0D, 12.0D));
        this.goalSelector.addGoal(2, new ResonarchPsychicAttackGoal(this));
        this.goalSelector.addGoal(5, new ResonarchRandomFloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHIELD_ACTIVE, false);
        this.entityData.define(COMBAT_STATE, STATE_IDLE);
    }

    public boolean isShieldActive() {
        return this.entityData.get(SHIELD_ACTIVE);
    }

    public int getCombatState() {
        return this.entityData.get(COMBAT_STATE);
    }

    private void setShieldActive(boolean value) {
        this.entityData.set(SHIELD_ACTIVE, value);
    }

    private void setCombatState(int state) {
        this.entityData.set(COMBAT_STATE, state);
    }

    public boolean isReadyForPsychicAttack() {
        return getCombatState() == STATE_IDLE && psychicCooldown <= 0;
    }

    public void onPsychicAttackUsed() {
        this.psychicCooldown = PSYCHIC_COOLDOWN_TICKS;
    }

    /** true si el mob se está desplazando de verdad (usado para elegir idle vs float). */
    public boolean isReallyMoving() {
        return reallyMoving;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        // Movimiento horizontal real, en vez de depender de isMoving() de GeckoLib
        Vec3 horizontal = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        this.reallyMoving = horizontal.lengthSqr() > MOVING_THRESHOLD_SQ;

        if (psychicCooldown > 0) psychicCooldown--;

        int state = getCombatState();
        if (state == STATE_DEFEND || state == STATE_DEFEND_IDLE) {
            shieldNoHitTicks++;
            if (shieldNoHitTicks >= SHIELD_TIMEOUT_TICKS) {
                startReflect();
            } else if (state == STATE_DEFEND && shieldNoHitTicks >= DEFEND_ANIM_TICKS) {
                setCombatState(STATE_DEFEND_IDLE);
            }
        } else if (state == STATE_REFLECT) {
            reflectTicks--;
            if (!reflectLaunched && reflectTicks <= REFLECT_DURATION_TICKS - LAUNCH_DELAY_TICKS) {
                launchFrozenProjectiles();
                reflectLaunched = true;
            }
            if (reflectTicks <= 0) {
                endShield();
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
        this.triggerAnim("bossController", "reflect");

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                    SoundSource.HOSTILE, 1.0F, 0.8F);
        }
    }

    private void endShield() {
        setShieldActive(false);
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
        if (getCombatState() == STATE_REFLECT) return;

        Vec3 anchor = projectile.position();
        projectile.setDeltaMovement(Vec3.ZERO);
        projectile.setNoGravity(true);
        projectile.hurtMarked = false;

        boolean alreadyTracked = frozenProjectiles.stream().anyMatch(fp -> fp.projectile == projectile);
        if (!alreadyTracked) {
            frozenProjectiles.add(new FrozenProjectile(projectile, anchor));
        }

        shieldNoHitTicks = 0;
        setShieldActive(true);
        if (getCombatState() != STATE_DEFEND_IDLE) {
            setCombatState(STATE_DEFEND);
            this.triggerAnim("bossController", "defend");
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !(source.getDirectEntity() instanceof Projectile) && super.hurt(source, amount);
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
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bossController", 5, this::predicate)
                .triggerableAnim("defend", RawAnimation.begin().thenPlay("defend"))
                .triggerableAnim("reflect", RawAnimation.begin().thenPlay("reflect")));
    }

    private PlayState predicate(AnimationState<ResonarchEntity> state) {
        switch (getCombatState()) {
            case STATE_DEFEND_IDLE -> state.getController().setAnimation(RawAnimation.begin().thenLoop("defend_idle"));
            case STATE_REFLECT -> state.getController().setAnimation(RawAnimation.begin().thenPlay("reflect"));
            case STATE_DEFEND -> state.getController().setAnimation(RawAnimation.begin().thenPlay("defend"));
            default -> {
                // Usa reallyMoving (calculado en tick() a partir del deltaMovement real)
                // en vez de state.isMoving(), que casi nunca detecta el desplazamiento
                // lento y flotante de este mob.
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

    // ---- Control de movimiento flotante ----
    static class ResonarchMoveControl extends MoveControl {
        private final ResonarchEntity resonarch;
        private int courseChangeCooldown;

        public ResonarchMoveControl(ResonarchEntity resonarch) {
            super(resonarch);
            this.resonarch = resonarch;
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) return;
            if (this.courseChangeCooldown-- > 0) return;

            this.courseChangeCooldown += this.resonarch.getRandom().nextInt(5) + 2;
            Vec3 vec3 = new Vec3(this.wantedX - this.resonarch.getX(),
                    this.wantedY - this.resonarch.getY(),
                    this.wantedZ - this.resonarch.getZ());
            double distance = vec3.length();
            vec3 = vec3.normalize();

            if (!this.canReach(vec3, Mth.ceil(distance))) {
                this.operation = MoveControl.Operation.WAIT;
                return;
            }

            double impulse = 0.08D * this.speedModifier;
            this.resonarch.setDeltaMovement(this.resonarch.getDeltaMovement().add(vec3.scale(impulse)));

            // Gira el cuerpo hacia la dirección real de movimiento (como FlyingMoveControl vanilla),
            // en vez de dejar que solo LookAtPlayerGoal decida hacia dónde "mira" el mob.
            if (vec3.x != 0.0D || vec3.z != 0.0D) {
                float targetYaw = (float) (Mth.atan2(vec3.z, vec3.x) * (180D / Math.PI)) - 90.0F;
                this.resonarch.setYRot(this.rotlerp(this.resonarch.getYRot(), targetYaw, 90.0F));
                this.resonarch.yBodyRot = this.resonarch.getYRot();
            }
        }

        private boolean canReach(Vec3 direction, int length) {
            AABB aabb = this.resonarch.getBoundingBox();
            for (int i = 1; i < length; ++i) {
                aabb = aabb.move(direction);
                if (!this.resonarch.level().noCollision(this.resonarch, aabb)) {
                    return false;
                }
            }
            return true;
        }
    }

    // ---- Deambular aleatorio ----
    static class ResonarchRandomFloatGoal extends Goal {
        private final ResonarchEntity resonarch;
        private int nextMoveTicks = 0;

        public ResonarchRandomFloatGoal(ResonarchEntity resonarch) {
            this.resonarch = resonarch;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.nextMoveTicks > 0) {
                this.nextMoveTicks--;
                return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = this.resonarch.getRandom();
            double targetX = this.resonarch.getX() + (random.nextDouble() * 12.0D - 6.0D);
            double targetY = this.resonarch.getY() + (random.nextDouble() * 4.0D - 2.0D);
            double targetZ = this.resonarch.getZ() + (random.nextDouble() * 12.0D - 6.0D);

            this.resonarch.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 0.3D);
            // Pausa aleatoria entre movimientos (1.5s a 3.5s)
            this.nextMoveTicks = 30 + random.nextInt(40);
        }
    }

    // ---- Mantener distancia del jugador ----
// ---- Mantener distancia del jugador ----
    static class ResonarchKeepDistanceGoal extends Goal {
        private static final int MAX_FLEE_TICKS = 40;          // huye como máximo ~2s por activación
        private static final int RETRIGGER_COOLDOWN_TICKS = 30; // ~1.5s antes de poder volver a huir

        private final ResonarchEntity resonarch;
        private final double minDistanceSq;
        private final double maxDistanceSq;
        private Player target;
        private int recalcCooldown = 0;
        private int fleeTicks = 0;
        private int retriggerCooldown = 0;

        public ResonarchKeepDistanceGoal(ResonarchEntity resonarch, double minDistance, double maxDistance) {
            this.resonarch = resonarch;
            this.minDistanceSq = minDistance * minDistance;
            this.maxDistanceSq = maxDistance * maxDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Cuenta el cooldown incluso cuando el goal no está activo
            if (this.retriggerCooldown > 0) {
                this.retriggerCooldown--;
                return false;
            }
            Player nearest = this.resonarch.level().getNearestPlayer(this.resonarch, 16.0D);
            return nearest != null && nearest.isAlive()
                    && this.resonarch.distanceToSqr(nearest) < this.minDistanceSq;
        }

        @Override
        public boolean canContinueToUse() {
            // Se corta por distancia segura O por tiempo máximo de huida, lo que ocurra primero
            return this.fleeTicks < MAX_FLEE_TICKS
                    && this.target != null && this.target.isAlive()
                    && this.resonarch.distanceToSqr(this.target) < this.maxDistanceSq;
        }

        @Override
        public void start() {
            this.target = this.resonarch.level().getNearestPlayer(this.resonarch, 16.0D);
            this.recalcCooldown = 0;
            this.fleeTicks = 0;
            moveAway();
        }

        @Override
        public void stop() {
            this.target = null;
            this.retriggerCooldown = RETRIGGER_COOLDOWN_TICKS;
        }

        @Override
        public void tick() {
            this.fleeTicks++;
            if (this.recalcCooldown-- <= 0) {
                this.recalcCooldown = 5;
                moveAway();
            }
        }

        private void moveAway() {
            if (this.target == null) return;
            Vec3 awayDir = this.resonarch.position().subtract(this.target.position()).normalize();
            Vec3 destination = this.resonarch.position().add(awayDir.scale(6.0D)).add(0, 1.5D, 0);
            this.resonarch.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 0.4D);
        }
    }
}
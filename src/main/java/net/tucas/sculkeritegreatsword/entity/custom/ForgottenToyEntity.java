package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ForgottenToySwellGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ForgottenToyEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR =
            SynchedEntityData.defineId(ForgottenToyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ALERT =
            SynchedEntityData.defineId(ForgottenToyEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int swell;
    private final int maxSwell = 30;       // ticks hasta explotar (~1.5s de mecha)
    private final float explosionRadius = 3.0F;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation EXPLODE = RawAnimation.begin().thenPlay("explote");

    public ForgottenToyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        // Mismo orden de prioridades que el Creeper vanilla
        this.goalSelector.addGoal(1, new ForgottenToySwellGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SWELL_DIR, -1);
        this.entityData.define(DATA_ALERT, false);
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            // Solo el servidor decide el estado de alerta; se sincroniza solo al cliente.
            if (!this.level().isClientSide) {
                this.setAlert(this.getTarget() != null);
            }

            int dir = this.getSwellDir();
            if (dir > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }

            this.swell = Mth.clamp(this.swell + dir, 0, this.maxSwell);

            if (this.swell >= this.maxSwell) {
                this.explodeToy();
            }
        }
        super.tick();
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int dir) {
        this.entityData.set(DATA_SWELL_DIR, dir);
    }

    /** true en cuanto empieza a encenderse (goal de swell activo) */
    public boolean isIgnited() {
        return this.swell > 0;
    }

    /** true cuando la entidad tiene un objetivo detectado (te vio) */
    public boolean isAlert() {
        return this.entityData.get(DATA_ALERT);
    }

    public void setAlert(boolean alert) {
        this.entityData.set(DATA_ALERT, alert);
    }

    private void explodeToy() {
        if (!this.level().isClientSide) {
            boolean grief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(
                    this, this.getX(), this.getY(), this.getZ(),
                    this.explosionRadius, false,
                    grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE
            );
            this.discard();
        }
    }

    // ---------------- GeckoLib ----------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "explosion", 0, this::explosionPredicate));
    }

    private PlayState movementPredicate(AnimationState<ForgottenToyEntity> state) {
        if (this.isIgnited()) {
            return PlayState.STOP;
        }
        state.getController().setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    private PlayState explosionPredicate(AnimationState<ForgottenToyEntity> state) {
        if (this.isIgnited()) {
            state.getController().setAnimation(EXPLODE);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
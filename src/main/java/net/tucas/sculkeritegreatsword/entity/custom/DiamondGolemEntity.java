package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.tucas.sculkeritegreatsword.entity.ai.goal.DiamondGolemProyectileAttackGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemFollowOwnerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DiamondGolemEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DIAMOND_ATTACK = RawAnimation.begin().thenPlay("diamond_attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // MELEE ATTACK
    private int attackAnimationTick = 0;
    private LivingEntity meleeTarget = null;
    private boolean meleeDamageDealt = false;

    // PROJECTILE ATTACK
    private int projectileAnimationTick = 0;

    public DiamondGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.5f);
        this.xpReward = 10;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DiamondGolemProyectileAttackGoal(this, 16.0, 8.0));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false));
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isTame() && this.isOwnedBy(player) && hand == InteractionHand.MAIN_HAND) {
            int currentMode = this.getPersistentData().getInt("golem_mode");
            int newMode = (currentMode % 3) + 1;
            this.getPersistentData().putInt("golem_mode", newMode);

            if (!this.level().isClientSide) {
                String msg = switch(newMode) {
                    case 1 -> "§aFOLLOW MODE";
                    case 2 -> "§eSTAY MODE";
                    case 3 -> "§bWANDER MODE";
                    default -> "";
                };
                player.displayClientMessage(Component.literal(msg), true);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.GLASS_HIT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            amount *= 0.5f;
        }

        boolean wasHurt = super.hurt(source, amount);

        if (wasHurt && source.getEntity() instanceof LivingEntity attacker) {
            if (!this.isOwnedBy(attacker)) {
                this.setTarget(attacker);
            }
        }

        return wasHurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity) {
            // 1.5 segundos = 30 ticks
            this.attackAnimationTick = 30;
            this.meleeTarget = (LivingEntity) target;
            this.meleeDamageDealt = false;

            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "attack");
            }

            return true;
        }

        return super.doHurtTarget(target);
    }

    // Método público para triggear la animación de proyectil desde DiamondGolemProyectileAttackGoal
    public void performProjectileAttack() {
        this.projectileAnimationTick = 40; // 2 segundos (duración de la animación)

        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("main", "diamond_attack");
        }
    }

    private void executeMeleeDamage() {
        if (this.meleeTarget != null && this.meleeTarget.isAlive() && !this.meleeDamageDealt) {
            this.meleeTarget.hurt(this.damageSources().mobAttack(this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));

            this.meleeDamageDealt = true;

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.IRON_GOLEM_ATTACK, this.getSoundSource(), 1.0f, 1.0f);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("AttackAnimationTick", this.attackAnimationTick);
        compound.putInt("ProjectileAnimationTick", this.projectileAnimationTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.attackAnimationTick = compound.getInt("AttackAnimationTick");
        this.projectileAnimationTick = compound.getInt("ProjectileAnimationTick");
        this.meleeTarget = null;
    }

    @Override
    public void tick() {
        super.tick();

        // ATAQUE MELEE
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;

            // Ejecutar el daño exactamente a la mitad de la animación
            // Animación: 1.5 segundos = 30 ticks
            // Mitad: 0.75 segundos = 15 ticks transcurridos
            // Como el contador va hacia abajo: 30 - 15 = 15 ticks restantes
            if (this.attackAnimationTick == 15) {
                this.executeMeleeDamage();
            }

            if (this.attackAnimationTick <= 0) {
                this.meleeTarget = null;
                this.meleeDamageDealt = false;
            }
        }

        // ATAQUE PROYECTIL
        if (this.projectileAnimationTick > 0) {
            this.projectileAnimationTick--;
        }
    }

    private PlayState mainAnimationController(AnimationState<DiamondGolemEntity> event) {
        // Prioridad a las animaciones de ataque
        if (this.attackAnimationTick > 0 || this.projectileAnimationTick > 0) {
            return PlayState.CONTINUE;
        }

        if (event.isMoving()) {
            return event.setAndContinue(WALK);
        }

        return event.setAndContinue(IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "main", 5, this::mainAnimationController)
                        .triggerableAnim("attack", ATTACK)
                        .triggerableAnim("diamond_attack", DIAMOND_ATTACK)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }
}
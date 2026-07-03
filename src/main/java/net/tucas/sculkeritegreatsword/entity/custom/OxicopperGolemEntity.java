package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.OxicopperGolemFleeFromEnemyGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.OxicopperGolemShellAttackGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.OxicopperGolemThunderGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemFollowOwnerGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class OxicopperGolemEntity extends TamableAnimal implements GeoEntity {

    // ========== DEFINIR ANIMACIONES COMO CONSTANTES ==========
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SHELL_IDLE = RawAnimation.begin().thenLoop("shell_idle");
    private static final RawAnimation FOLD = RawAnimation.begin().thenPlay("fold");
    private static final RawAnimation UNFOLD = RawAnimation.begin().thenPlay("unfold");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation THUNDER = RawAnimation.begin().thenPlay("shot_thunder");

    // ========== ENTITY DATA ACCESSORS ==========
    private static final EntityDataAccessor<Boolean> IN_SHELL =
            SynchedEntityData.defineId(OxicopperGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EMISSIVE =
            SynchedEntityData.defineId(OxicopperGolemEntity.class, EntityDataSerializers.BOOLEAN);

    // ========== CACHE DE ANIMACIÓN ==========
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ========== VARIABLES DE CONTROL ==========
    private int shootAnimTicks = 0;
    private LivingEntity shootTarget = null;
    private int shootCooldown = 0;

    // Sistema de ataques recibidos
    private int consecutiveHits = 0;
    private int hitResetTimer = 0;
    private static final int HIT_RESET_TIME = 60;
    private static final int HITS_TO_FORCE_MOVE = 4;

    // ========== SISTEMA DE TRUENOS ==========
    private int thunderCooldown = 0;
    private static final int THUNDER_COOLDOWN = 300; // 15 segundos
    private int thunderAnimTicks = 0;
    private List<LivingEntity> thunderTargets = new ArrayList<>();
    private boolean thunderInvulnerable = false;
    private int emissiveFlickerTimer = 0;

    // Timing para disparos múltiples
    private static final int[] THUNDER_STRIKE_TICKS = {15, 25, 35}; // Ticks 0.75s, 1.25s, 1.75s
    private int nextStrikeIndex = 0;

    // ========== CONSTRUCTOR ==========
    public OxicopperGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(false);
        this.setMaxUpStep(0.6f);
        this.setPersistenceRequired();
    }

    // ========== DEFINE SYNCED DATA ==========
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IN_SHELL, false);
        this.entityData.define(IS_EMISSIVE, false);
    }

    // ========== GETTERS Y SETTERS ==========
    public boolean isInShell() {
        return this.entityData.get(IN_SHELL);
    }

    private void setInShell(boolean inShell) {
        this.entityData.set(IN_SHELL, inShell);
    }

    // PÚBLICO para que el Renderer y Model puedan acceder
    public boolean isEmissive() {
        return this.entityData.get(IS_EMISSIVE);
    }

    private void setEmissive(boolean emissive) {
        this.entityData.set(IS_EMISSIVE, emissive);
    }

    // ========== NETWORK ==========
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ========== ATRIBUTOS ==========
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 6.0D);
    }

    // ========== REGISTER GOALS ==========
    @Override
    protected void registerGoals() {
        // GOAL DE TRUENO (Prioridad 0 - Máxima)
        this.goalSelector.addGoal(0, new OxicopperGolemThunderGoal(this, 20.0, 3));

        this.goalSelector.addGoal(1, new OxicopperGolemFleeFromEnemyGoal(this, 1.2, 5.0f));
        this.goalSelector.addGoal(2, new OxicopperGolemShellAttackGoal(this, 16.0, 6.0));
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false)); // CAMBIO AQUÍ
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Slime.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
    }

    // ========== MOB TYPE ==========
    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    // ========== PERSISTENCIA ==========
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // ========== SONIDOS ==========
    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    // ========== DAÑO ==========
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Invulnerable durante ataque de trueno
        if (this.thunderInvulnerable) {
            return false;
        }

        // Inmune a rayos (incluyendo los suyos propios)
        if (source.is(DamageTypes.LIGHTNING_BOLT)) {
            return false;
        }

        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }

        if (this.isInShell()) {
            amount *= 0.2f;
        }

        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity) {
            this.consecutiveHits++;
            this.hitResetTimer = HIT_RESET_TIME;

            if (this.consecutiveHits >= HITS_TO_FORCE_MOVE && this.isInShell()) {
                this.forceExitShell();
            }
        }

        return super.hurt(source, amount);
    }

    // ========== NBT (GUARDAR/CARGAR) ==========
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("InShell", this.isInShell());
        compound.putInt("ShootCooldown", this.shootCooldown);
        compound.putInt("ShootAnimTicks", this.shootAnimTicks);
        compound.putInt("ConsecutiveHits", this.consecutiveHits);
        compound.putInt("HitResetTimer", this.hitResetTimer);
        compound.putInt("ThunderCooldown", this.thunderCooldown);
        compound.putInt("ThunderAnimTicks", this.thunderAnimTicks);
        compound.putBoolean("ThunderInvulnerable", this.thunderInvulnerable);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setInShell(compound.getBoolean("InShell"));
        this.shootCooldown = compound.getInt("ShootCooldown");
        this.shootAnimTicks = compound.getInt("ShootAnimTicks");
        this.consecutiveHits = compound.getInt("ConsecutiveHits");
        this.hitResetTimer = compound.getInt("HitResetTimer");
        this.thunderCooldown = compound.getInt("ThunderCooldown");
        this.thunderAnimTicks = compound.getInt("ThunderAnimTicks");
        this.thunderInvulnerable = compound.getBoolean("ThunderInvulnerable");
        this.shootTarget = null;
        this.thunderTargets.clear();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            return this.isTame() && this.isOwnedBy(player) || this.isFood(itemstack) ?
                    InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        // CAMBIO DE MODO
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
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COPPER_INGOT);
    }

    // ========== BREEDING ==========
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    // ========== TICK PRINCIPAL ==========
    @Override
    public void tick() {
        super.tick();

        // Cooldown de disparo normal
        if (this.shootCooldown > 0) {
            this.shootCooldown--;
        }

        // Cooldown de trueno
        if (this.thunderCooldown > 0) {
            this.thunderCooldown--;
        }

        // Reset de contador de golpes
        if (this.hitResetTimer > 0) {
            this.hitResetTimer--;
            if (this.hitResetTimer <= 0) {
                this.consecutiveHits = 0;
            }
        }

        // ANIMACIÓN DE DISPARO NORMAL
        if (this.shootAnimTicks > 0) {
            this.shootAnimTicks--;
            if (this.shootAnimTicks == 11 && this.shootTarget != null && this.shootTarget.isAlive()) {
                this.executeShoot();
            }
            if (this.shootAnimTicks <= 0) {
                this.shootTarget = null;
            }
        }

        // ========== ANIMACIÓN DE TRUENO ==========
        if (this.thunderAnimTicks > 0) {
            this.thunderAnimTicks--;

            // Parpadeo de textura emisiva (cada 2 ticks)
            this.emissiveFlickerTimer++;
            if (this.emissiveFlickerTimer % 2 == 0) {
                this.setEmissive(!this.isEmissive());
            }

            // Ejecutar rayos en los momentos específicos
            if (this.nextStrikeIndex < THUNDER_STRIKE_TICKS.length) {
                int currentStrikeTick = THUNDER_STRIKE_TICKS[this.nextStrikeIndex];
                int ticksRemaining = 40 - this.thunderAnimTicks; // Convertir countdown a count-up

                if (ticksRemaining == currentStrikeTick) {
                    this.executeThunderStrike(this.nextStrikeIndex);
                    this.nextStrikeIndex++;
                }
            }

            // Al terminar la animación
            if (this.thunderAnimTicks <= 0) {
                this.thunderTargets.clear();
                this.thunderInvulnerable = false;
                this.setEmissive(false);
                this.emissiveFlickerTimer = 0;
                this.nextStrikeIndex = 0;
            }
        }
    }

    // ========== BLOQUEAR MOVIMIENTO EN SHELL Y TRUENO ==========
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInShell() || this.thunderInvulnerable) {
            this.getNavigation().stop();
            super.travel(new Vec3(0, travelVector.y, 0));
            return;
        }
        super.travel(travelVector);
    }

    // ========== MÉTODOS DE CONTROL - SHELL ==========
    public void enterShell() {
        if (!this.isInShell()) {
            this.setInShell(true);
            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "fold");
            }
        }
    }

    public void exitShell() {
        if (this.isInShell()) {
            this.setInShell(false);
            this.consecutiveHits = 0;
            this.hitResetTimer = 0;
            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "unfold");
            }
        }
    }

    private void forceExitShell() {
        this.setInShell(false);
        this.consecutiveHits = 0;
        this.hitResetTimer = 0;
        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("main", "unfold");
        }
    }

    // ========== MÉTODOS DE DISPARO NORMAL ==========
    public void startShootSequence(LivingEntity target) {
        if (this.shootCooldown <= 0 && !this.isShooting()) {
            this.shootTarget = target;
            this.shootAnimTicks = 22;
            this.shootCooldown = 25;

            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "shoot");
            }
        }
    }

    private void executeShoot() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.shootTarget == null) {
            return;
        }

        Vec3 start = this.position().add(0, this.getBbHeight() * 0.5, 0);
        Vec3 end = this.shootTarget.position().add(0, this.shootTarget.getBbHeight() * 0.5, 0);

        for (int i = 0; i < 15; i++) {
            double t = i / 15.0;
            double x = start.x + (end.x - start.x) * t;
            double y = start.y + (end.y - start.y) * t;
            double z = start.z + (end.z - start.z) * t;

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 2, 0.1, 0.1, 0.1, 0.02);
        }

        this.shootTarget.hurt(this.damageSources().mobAttack(this), 10.0f);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, this.getSoundSource(), 0.5f, 1.5f);
    }

    // ========== MÉTODOS DE ATAQUE DE TRUENO (LLAMADOS POR EL GOAL) ==========

    public boolean canUseThunder() {
        return this.thunderCooldown <= 0 && this.thunderAnimTicks <= 0 && !this.isUsingThunder();
    }

    public boolean isUsingThunder() {
        return this.thunderAnimTicks > 0;
    }

    public List<LivingEntity> getThunderTargets() {
        return this.thunderTargets;
    }

    /**
     * Inicia el ataque de trueno con múltiples objetivos
     */
    public void startThunderAttack(List<LivingEntity> targets) {
        if (targets.isEmpty() || !this.canUseThunder()) {
            return;
        }

        this.thunderTargets = new ArrayList<>(targets);
        this.thunderAnimTicks = 40; // 2 segundos
        this.thunderCooldown = THUNDER_COOLDOWN; // 15 segundos
        this.thunderInvulnerable = true;
        this.emissiveFlickerTimer = 0;
        this.nextStrikeIndex = 0;

        if (this.level() instanceof ServerLevel) {
            this.triggerAnim("main", "thunder");
        }
    }

    /**
     * Ejecuta un rayo individual en el momento específico
     */
    private void executeThunderStrike(int targetIndex) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Verificar que el índice sea válido
        if (targetIndex >= this.thunderTargets.size()) {
            return;
        }

        LivingEntity target = this.thunderTargets.get(targetIndex);

        // Verificar que el objetivo siga vivo
        if (target == null || !target.isAlive()) {
            return;
        }

        // Invocar rayo real
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(target.getX(), target.getY(), target.getZ());
            serverLevel.addFreshEntity(lightning);
        }

        // Partículas eléctricas desde el golem al objetivo
        Vec3 start = this.position().add(0, this.getBbHeight() * 0.5, 0);
        Vec3 end = target.position().add(0, target.getBbHeight() * 0.5, 0);

        for (int i = 0; i < 30; i++) {
            double t = i / 30.0;
            double x = start.x + (end.x - start.x) * t;
            double y = start.y + (end.y - start.y) * t;
            double z = start.z + (end.z - start.z) * t;

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 5, 0.2, 0.2, 0.2, 0.05);
        }

        // Daño potente
        target.hurt(this.damageSources().lightningBolt(), 15.0f);

        // Sonido de trueno
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, this.getSoundSource(), 1.5f, 1.0f + (targetIndex * 0.1f));
    }

    public boolean isShooting() {
        return this.shootAnimTicks > 0;
    }

    public boolean canShoot() {
        return this.isInShell() && this.shootCooldown <= 0 && !this.isShooting();
    }

    public boolean shouldStayInShell() {
        return this.consecutiveHits < HITS_TO_FORCE_MOVE;
    }

    // ========== KNOCKBACK Y FLUIDOS ==========
    @Override
    public void knockback(double strength, double x, double z) {
        if (!this.isInShell() && !this.thunderInvulnerable) {
            super.knockback(strength * 0.5, x, z);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    // ========== CONTROLADOR DE ANIMACIÓN ==========
    private PlayState mainAnimationController(AnimationState<OxicopperGolemEntity> event) {
        AnimationController<OxicopperGolemEntity> controller = event.getController();

        if (this.isInShell()) {
            return event.setAndContinue(SHELL_IDLE);
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
                        .triggerableAnim("shoot", SHOOT)
                        .triggerableAnim("fold", FOLD)
                        .triggerableAnim("unfold", UNFOLD)
                        .triggerableAnim("thunder", THUNDER)
        );
    }
    public int getConsecutiveHits() {
        return this.consecutiveHits;
    }

    public void resetConsecutiveHits() {
        this.consecutiveHits = 0;
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
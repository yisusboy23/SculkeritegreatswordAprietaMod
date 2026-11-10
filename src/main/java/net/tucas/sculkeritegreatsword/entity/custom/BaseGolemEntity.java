package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemFollowOwnerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemTargetMonsterGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemTargetPillagerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Base de TODOS los golems del mod.
 *
 * Centraliza lo que antes estaba copiado en cada clase:
 *  - modo FOLLOW / STAY / WANDER (guardado en getPersistentData "golem_mode")
 *  - mobInteract (cambiar modo, curar, domesticar)
 *  - ataque melee con animación y daño retrasado
 *  - goals comunes (flotar, seguir, vagar, mirar, targets del dueño)
 *  - sonidos, atributos, inmunidad a caída, sin crianza
 *  - controlador de animación "main" con idle/walk/attack
 *
 * IMPORTANTE: registerGoals() se llama desde el constructor de Mob, ANTES de que
 * se inicialicen los campos de las subclases. Los hooks que se usan ahí
 * (registerCombatGoals, hasMeleeAnimation...) NO deben leer campos de instancia.
 */
public abstract class BaseGolemEntity extends TamableAnimal implements GeoEntity {

    // ======================= CONSTANTES COMPARTIDAS =======================
    public static final String MODE_KEY = "golem_mode";
    public static final int MODE_FOLLOW = 1;
    public static final int MODE_STAY = 2;
    public static final int MODE_WANDER = 3;

    protected static final String CONTROLLER_MAIN = "main";

    // Valores de movimiento estándar (cámbialos aquí y cambian en TODOS los golems)
    protected static final double FOLLOW_SPEED = 0.7;
    protected static final float FOLLOW_START_DISTANCE = 10.0f;
    protected static final float FOLLOW_STOP_DISTANCE = 3.0f;
    protected static final double WANDER_SPEED = 0.6;
    protected static final double MELEE_SPEED = 1.0;

    // Animaciones comunes
    protected static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation ANIM_RUN = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("attack");

    // true mientras se reproduce CUALQUIER animación de acción (sincronizado al cliente)
    public static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Estado de la acción actual (solo servidor)
    private int actionTicks = 0;
    @Nullable
    private LivingEntity meleeTarget = null;

    protected BaseGolemEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setCanPickUpLoot(false);
        this.setPersistenceRequired();
    }

    // ======================= ATRIBUTOS =======================

    /** Builder estándar. Siempre incluye ATTACK_DAMAGE y ATTACK_KNOCKBACK (evita crashes por atributo faltante). */
    protected static AttributeSupplier.Builder createGolemAttributes(double health, double speed, double armor,
                                                                     double attackDamage, double followRange,
                                                                     double knockbackResistance) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.FOLLOW_RANGE, followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance);
    }

    // ======================= DATOS / RED =======================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ======================= GOALS =======================

    @Override
    protected final void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Goals propios de cada golem (prioridades 0-2)
        this.registerCombatGoals();

        if (this.hasMeleeAnimation()) {
            this.goalSelector.addGoal(2, new GolemMeleeAttackGoal(this, MELEE_SPEED));
        }

        // Comportamiento estándar (los goals SculkGolem* revisan "golem_mode")
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, FOLLOW_SPEED,
                FOLLOW_START_DISTANCE, FOLLOW_STOP_DISTANCE, false));
        this.goalSelector.addGoal(4, new SculkGolemWanderGoal(this, WANDER_SPEED));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.registerHurtByTargetGoal();
        if (this.targetsHostileMobs()) {
            // Pillagers primero (prioridad 4), luego cualquier Monster (prioridad 5).
            // Ambos respetan AttackCheckControlProcedure.
            this.targetSelector.addGoal(4, new SculkGolemTargetPillagerGoal(this));
            this.targetSelector.addGoal(5, new SculkGolemTargetMonsterGoal(this));
        }
        this.registerExtraTargetGoals();
    }

    /** Goals de ataque/habilidad propios (usa prioridades 0, 1 y 2). */
    protected void registerCombatGoals() {}

    /** Goal de "me golpearon" (prioridad 3). Sobrescribir si el golem usa uno propio. */
    protected void registerHurtByTargetGoal() {
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    /** Targets extra (usa prioridad 6 o mayor). */
    protected void registerExtraTargetGoals() {}

    /** ¿Ataca automáticamente a Pillagers y a cualquier Monster cercano? */
    protected boolean targetsHostileMobs() {
        return true;
    }

    /** MeleeAttackGoal que no golpea mientras hay una animación en curso, pero SIGUE persiguiendo. */
    protected static class GolemMeleeAttackGoal extends MeleeAttackGoal {
        private final BaseGolemEntity golem;

        public GolemMeleeAttackGoal(BaseGolemEntity golem, double speed) {
            super(golem, speed, true);
            this.golem = golem;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            if (this.golem.isActionAnimating()) {
                return;
            }
            super.checkAndPerformAttack(enemy, distToEnemySqr);
        }
    }

    // ======================= MODO FOLLOW / STAY / WANDER =======================

    public int getGolemMode() {
        int mode = this.getPersistentData().getInt(MODE_KEY);
        return (mode >= MODE_FOLLOW && mode <= MODE_WANDER) ? mode : MODE_FOLLOW;
    }

    public void setGolemMode(int mode) {
        this.getPersistentData().putInt(MODE_KEY, mode);
    }

    protected void cycleMode(Player player) {
        int next = this.getGolemMode() % 3 + 1;
        this.setGolemMode(next);

        Component msg = switch (next) {
            case MODE_STAY -> Component.literal("STAY MODE").withStyle(ChatFormatting.YELLOW);
            case MODE_WANDER -> Component.literal("WANDER MODE").withStyle(ChatFormatting.AQUA);
            default -> Component.literal("FOLLOW MODE").withStyle(ChatFormatting.GREEN);
        };
        player.displayClientMessage(msg, true);
    }

    /** Todo golem domesticado arranca en FOLLOW. */
    @Override
    public void tame(Player player) {
        super.tame(player);
        this.setGolemMode(MODE_FOLLOW);
    }

    /** Golems domesticados por otras vías (spawn, comandos, invocaciones) también quedan en FOLLOW. */
    private void ensureDefaultMode() {
        if (this.isTame() && this.getPersistentData().getInt(MODE_KEY) == 0) {
            this.setGolemMode(MODE_FOLLOW);
        }
    }

    // ======================= INTERACCIÓN =======================

    /** Items para domesticar. Por defecto = isFood(). */
    protected boolean isTameItem(ItemStack stack) {
        return this.isFood(stack);
    }

    /** Items para curar al golem (solo dueño). Por defecto = isFood(). */
    protected boolean isHealItem(ItemStack stack, Player player) {
        return this.isFood(stack);
    }

    protected float getHealAmount() {
        return 20.0f;
    }

    /** Efectos al curar (solo servidor). */
    protected void onHealed(ServerLevel level) {
        level.broadcastEntityEvent(this, (byte) 7); // corazones
    }

    /**
     * Interacciones propias del dueño (ej: guardar pociones).
     * Devuelve null si NO manejó la interacción.
     */
    @Nullable
    protected InteractionResult onOwnerInteract(Player player, InteractionHand hand, ItemStack stack) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = this.level().isClientSide;

        if (this.isTame()) {
            if (!this.isOwnedBy(player)) {
                return super.mobInteract(player, hand);
            }

            // 1) Interacciones específicas del golem
            InteractionResult custom = this.onOwnerInteract(player, hand, stack);
            if (custom != null) {
                return custom;
            }

            // 2) Curar
            if (this.isHealItem(stack, player)) {
                return this.tryHeal(player, stack);
            }

            // 3) Cambiar modo: mano principal VACÍA y sin shift
            if (hand == InteractionHand.MAIN_HAND && stack.isEmpty() && !player.isShiftKeyDown()) {
                if (!clientSide) {
                    this.cycleMode(player);
                }
                return InteractionResult.sidedSuccess(clientSide);
            }
            return super.mobInteract(player, hand);
        }

        // Sin dueño: intentar domesticar
        if (this.isTameItem(stack)) {
            return this.tryTame(player, stack);
        }
        return super.mobInteract(player, hand);
    }

    private InteractionResult tryHeal(Player player, ItemStack stack) {
        if (this.getHealth() >= this.getMaxHealth()) {
            return InteractionResult.PASS;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.heal(this.getHealAmount());
            this.onHealed(serverLevel);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private InteractionResult tryTame(Player player, ItemStack stack) {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.setTarget(null);
                serverLevel.broadcastEntityEvent(this, (byte) 7);
                player.displayClientMessage(Component.empty()
                        .append(this.getType().getDescription())
                        .append(Component.literal(" tamed! Mode: FOLLOW"))
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                serverLevel.broadcastEntityEvent(this, (byte) 6);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ======================= ALIADOS =======================

    /** Dueño o mascota del mismo dueño. */
    protected boolean isAllyOrOwner(LivingEntity entity) {
        if (this.isOwnedBy(entity)) {
            return true;
        }
        UUID ownerId = this.getOwnerUUID();
        return ownerId != null
                && entity instanceof OwnableEntity ownable
                && ownerId.equals(ownable.getOwnerUUID());
    }

    // ======================= ACCIONES / ANIMACIONES DE ATAQUE =======================

    /** ¿Este golem usa el ataque melee con animación y daño retrasado? */
    protected boolean hasMeleeAnimation() {
        return false;
    }

    /** Duración total de la animación melee (ticks). */
    protected int getMeleeDuration() {
        return 30;
    }

    /** Ticks RESTANTES en los que cae el golpe (el contador va hacia abajo). */
    protected int getMeleeHitTick() {
        return 15;
    }

    protected double getMeleeReach() {
        return 3.5;
    }

    @Nullable
    protected SoundEvent getMeleeHitSound() {
        return SoundEvents.IRON_GOLEM_ATTACK;
    }

    /** Se llama tras aplicar el golpe melee. */
    protected void onMeleeHit(LivingEntity target) {}

    @Nullable
    protected LivingEntity getMeleeTarget() {
        return this.meleeTarget;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!this.hasMeleeAnimation() || !(target instanceof LivingEntity living)) {
            return super.doHurtTarget(target);
        }
        if (this.isActionAnimating()) {
            return false;
        }
        this.startMeleeAction(living);
        return true;
    }

    /** Inicia la animación melee y programa el golpe sobre el objetivo (usa getMeleeDuration/getMeleeHitTick). */
    protected void startMeleeAction(LivingEntity target) {
        this.startAction("attack", this.getMeleeDuration());
        this.meleeTarget = target;
    }

    /** Compatibilidad con goals que llaman a este método. */
    public void triggerMeleeAttackAnimation() {
        this.startAction("attack", this.getMeleeDuration());
    }

    /** Inicia una animación de acción: marca ATTACKING (sincronizado) y dispara el trigger de GeckoLib. */
    protected void startAction(String triggerName, int durationTicks) {
        this.meleeTarget = null;
        this.actionTicks = durationTicks;
        this.setAttacking(true);
        if (this.level() instanceof ServerLevel) {
            this.triggerAnim(CONTROLLER_MAIN, triggerName);
        }
    }

    public boolean isActionAnimating() {
        return this.actionTicks > 0;
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    /** Se llama cada tick mientras dura una acción (ticks restantes, ya decrementado). */
    protected void onActionTick(int remainingTicks) {}

    private void performMeleeHit(LivingEntity target) {
        if (!target.isAlive() || this.distanceTo(target) >= this.getMeleeReach()) {
            return;
        }
        if (super.doHurtTarget(target)) {
            SoundEvent sound = this.getMeleeHitSound();
            if (sound != null) {
                this.playSound(sound, 1.0f, 1.0f);
            }
            this.onMeleeHit(target);
        }
    }

    private void tickAction() {
        if (this.actionTicks <= 0) {
            return;
        }
        this.actionTicks--;

        if (this.meleeTarget != null && this.actionTicks == this.getMeleeHitTick()) {
            this.performMeleeHit(this.meleeTarget);
        }
        this.onActionTick(this.actionTicks);

        if (this.actionTicks <= 0) {
            this.meleeTarget = null;
            this.setAttacking(false);
        }
    }

    // ======================= TICK =======================

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.ensureDefaultMode();
        this.tickAction();
        this.serverTick();
    }

    /** Lógica por tick SOLO en servidor. Las subclases sobrescriben esto en vez de tick(). */
    protected void serverTick() {}

    /** Helper para cooldowns guardados en getPersistentData (usados por goals externos). */
    protected void tickPersistentCooldown(String key) {
        CompoundTag data = this.getPersistentData();
        int value = data.getInt(key);
        if (value > 0) {
            data.putInt(key, value - 1);
        }
    }

    // ======================= MOVIMIENTO / DAÑO =======================

    /** Si es true el golem no se desplaza (sí cae por gravedad). */
    protected boolean shouldFreezeMovement() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.shouldFreezeMovement()) {
            this.getNavigation().stop();
            super.travel(new Vec3(0, travelVector.y, 0));
            return;
        }
        super.travel(travelVector);
    }

    /** Todos los golems son inmunes al daño por caída. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    // ======================= SIN CRIANZA =======================

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    // ======================= VARIOS =======================

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    // ======================= ANIMACIÓN (GeckoLib) =======================

    /** Animación que reemplaza a idle/walk según el estado (null = usar idle/walk). */
    @Nullable
    protected RawAnimation getStateAnimation(AnimationState<BaseGolemEntity> state) {
        return null;
    }

    /** Para animaciones largas que no usan startAction (ej: rodar). Debe leer datos SINCRONIZADOS. */
    protected boolean isPlayingCustomAction() {
        return false;
    }

    /** Registra aquí los triggerableAnim propios de cada golem (el "attack" ya viene de serie). */
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {}

    protected PlayState animationPredicate(AnimationState<BaseGolemEntity> state) {
        if (this.isAttacking() || this.isPlayingCustomAction()) {
            return PlayState.CONTINUE;
        }
        RawAnimation override = this.getStateAnimation(state);
        if (override != null) {
            return state.setAndContinue(override);
        }
        return state.setAndContinue(state.isMoving() ? ANIM_WALK : ANIM_IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<BaseGolemEntity> controller =
                new AnimationController<>(this, CONTROLLER_MAIN, 5, this::animationPredicate);
        controller.triggerableAnim("attack", ANIM_ATTACK);
        this.registerTriggerableAnims(controller);
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
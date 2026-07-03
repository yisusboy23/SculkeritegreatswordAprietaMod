package net.tucas.sculkeritegreatsword.entity.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.tucas.sculkeritegreatsword.entity.ai.goal.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.procedures.SculkeritegreatswordkineticenergydashEffectExpiresProcedure;
import net.tucas.sculkeritegreatsword.procedures.SculkGolemDeathExplosionProcedure;
import net.tucas.sculkeritegreatsword.procedures.AttackCheckControlProcedure;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemWanderGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.network.chat.Component;

public class SculkGolemEntity extends TamableAnimal implements GeoEntity {

    // ========== DEFINIR ANIMACIONES COMO CONSTANTES ==========
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation SONIC_BOOM = RawAnimation.begin().thenPlay("attack_sonic_boom");

    // ========== ENTITY DATA ACCESSORS ==========
    public static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> USING_SONIC_BOOM = SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.STRING);

    // ========== CACHE DE ANIMACIÓN ==========
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ========== VARIABLES DE CONTROL - ESTILO MELEE ==========
    private int attackAnimTicks = 0; // Contador de animación de ataque normal
    private int sonicBoomAnimTicks = 0; // Contador de animación de sonic boom
    private LivingEntity attackTarget = null; // Target del ataque
    private int attackCooldown = 0; // Cooldown entre ataques

    // Sistema de conteo de ataques por enemigo
    private Map<UUID, Integer> enemyAttackCount = new HashMap<>();

    public SculkGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.SCULK_GOLEM.get(), world);
    }

    public SculkGolemEntity(EntityType<SculkGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setCanPickUpLoot(false);
        this.setMaxUpStep(0.6f);
        this.setPersistenceRequired();
        this.getPersistentData().putBoolean("attack_hostiles", true);
        this.getPersistentData().putBoolean("defense", false);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(USING_SONIC_BOOM, false);
        this.entityData.define(TEXTURE, "sculk_golem");

        if (!this.getPersistentData().contains("attack_hostiles")) {
            this.getPersistentData().putBoolean("attack_hostiles", true);
        }
        if (!this.getPersistentData().contains("defense")) {
            this.getPersistentData().putBoolean("defense", false);
        }
    }


    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SculkGolemSonicBoomGoal(this, 20.0, 300)); // ⚡ Rango aumentado a 20 bloques
        this.goalSelector.addGoal(3, new SculkGolemFollowOwnerGoal(this, 0.7, 10.0f, 3.0f, false));
        this.goalSelector.addGoal(4, new SculkGolemMeleeGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new SculkGolemWanderGoal(this, 0.6)); // Solo activo en WANDER mode (3)
        // ❌ ELIMINADO RandomStrollGoal - no respeta STAY mode
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new SculkGolemHurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, (entity) -> {
            return AttackCheckControlProcedure.execute(this) &&
                    entity instanceof Monster &&
                    !(entity.getType().toString().toLowerCase().contains("villager"));
        }));
        this.targetSelector.addGoal(5, new SculkGolemTargetPillagerGoal(this));
        this.targetSelector.addGoal(6, new SculkGolemTargetSlimeGoal(this));
    }


    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Texture", this.getTexture());
        compound.putBoolean("attack_hostiles", this.getPersistentData().getBoolean("attack_hostiles"));
        compound.putBoolean("defense", this.getPersistentData().getBoolean("defense"));
        compound.putInt("AttackCooldown", this.attackCooldown);
        compound.putInt("AttackAnimTicks", this.attackAnimTicks);
        compound.putInt("SonicBoomAnimTicks", this.sonicBoomAnimTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Texture")) {
            this.setTexture(compound.getString("Texture"));
        }
        if (compound.contains("attack_hostiles")) {
            this.getPersistentData().putBoolean("attack_hostiles", compound.getBoolean("attack_hostiles"));
        } else {
            this.getPersistentData().putBoolean("attack_hostiles", true);
        }
        if (compound.contains("defense")) {
            this.getPersistentData().putBoolean("defense", compound.getBoolean("defense"));
        } else {
            this.getPersistentData().putBoolean("defense", false);
        }
        this.attackCooldown = compound.getInt("AttackCooldown");
        this.attackAnimTicks = compound.getInt("AttackAnimTicks");
        this.sonicBoomAnimTicks = compound.getInt("SonicBoomAnimTicks");
        this.attackTarget = null;
    }

    @Override
    public InteractionResult mobInteract(Player sourceentity, InteractionHand hand) {
        ItemStack itemstack = sourceentity.getItemInHand(hand);
        InteractionResult retval = InteractionResult.sidedSuccess(this.level().isClientSide);

        // CASO 1: HUEVO DE SPAWN
        if (itemstack.getItem() instanceof SpawnEggItem) {
            retval = super.mobInteract(sourceentity, hand);
        }
        // CASO 2: CLIENTE (tu pantalla)
        else if (this.level().isClientSide) {
            retval = this.isTame() && this.isOwnedBy(sourceentity) || this.isFood(itemstack) ?
                    InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
        }
        // CASO 3: CAMBIO DE MODO
        else if (this.isTame() && this.isOwnedBy(sourceentity) && hand == InteractionHand.MAIN_HAND) {
            int currentMode = this.getPersistentData().getInt("golem_mode");
            int newMode = (currentMode % 3) + 1;
            this.getPersistentData().putInt("golem_mode", newMode);

            String msg = switch(newMode) {
                case 1 -> "§aFOLLOW MODE";
                case 2 -> "§eSTAY MODE";
                case 3 -> "§bWANDER MODE";
                default -> "";
            };
            sourceentity.displayClientMessage(Component.literal(msg), true);

            retval = InteractionResult.SUCCESS;
        }
        else {
            retval = super.mobInteract(sourceentity, hand);
        }

        return retval;
    }

    @Override
    public SculkGolemEntity getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
        SculkGolemEntity retval = ModEntities.SCULK_GOLEM.get().create(serverWorld);
        retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null, null);
        return retval;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SCULK);
    }

    // ========== MÉTODOS DE CONTROL DE ATAQUES ==========

    /**
     * Inicia secuencia de ataque normal - Estilo melee
     */
    public void startAttackSequence(LivingEntity target) {
        if (this.attackCooldown <= 0 && !this.isAttacking() && !this.isUsingSonicBoom()) {
            this.attackTarget = target;
            this.attackAnimTicks = 20; // Duración de la animación attack (1 segundo)
            this.attackCooldown = 40; // Cooldown antes del próximo ataque

            // ⚡ SINCRONIZAR CON CLIENTE
            this.entityData.set(ATTACKING, true);

            // Trigger animación attack
            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "attack");
            }
        }
    }

    /**
     * Ejecuta el daño REAL del ataque normal (en el tick 10)
     */
    private void performNormalAttack() {
        if (!(this.level() instanceof ServerLevel) || this.attackTarget == null) {
            return;
        }

        if (this.attackTarget.isAlive()) {
            this.attackTarget.hurt(this.damageSources().mobAttack(this), 16.0f);

            // Incrementar contador de ataques
            UUID targetUUID = this.attackTarget.getUUID();
            int currentAttacks = this.enemyAttackCount.getOrDefault(targetUUID, 0) + 1;
            this.enemyAttackCount.put(targetUUID, currentAttacks);
        }
    }

    /**
     * Inicia secuencia de Sonic Boom - Estilo melee
     */
    public void startSonicBoomSequence() {
        if (!this.isAttacking() && !this.isUsingSonicBoom()) {
            this.sonicBoomAnimTicks = 47; // Duración de la animación sonic boom (2.3333 segundos = 47 ticks)

            // ⚡ SINCRONIZAR CON CLIENTE - ESTO ES CRÍTICO
            this.entityData.set(USING_SONIC_BOOM, true);

            // Trigger animación sonic_boom
            if (this.level() instanceof ServerLevel) {
                this.triggerAnim("main", "sonic_boom");
            }
        }
    }

    /**
     * Ejecuta el Sonic Boom REAL (a mitad de la animación)
     */
    private void executeSonicBoom() {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }

        SculkeritegreatswordkineticenergydashEffectExpiresProcedure.execute(
                this.level(),
                this.getX(),
                this.getY(),
                this.getZ(),
                this
        );

        // Limpiar contadores de enemigos
        this.enemyAttackCount.clear();
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public boolean isUsingSonicBoom() {
        return this.entityData.get(USING_SONIC_BOOM);
    }

    public boolean canAttack() {
        return this.attackCooldown <= 0 && !this.isAttacking() && !this.isUsingSonicBoom();
    }

    public boolean canUseSonicBoom() {
        int cooldown = this.getPersistentData().getInt("sonicCooldown");
        return cooldown <= 0 && !this.isAttacking() && !this.isUsingSonicBoom();
    }

    public int getAttackCountForEnemy(UUID enemyUUID) {
        return this.enemyAttackCount.getOrDefault(enemyUUID, 0);
    }

    // ========== BLOQUEAR MOVIMIENTO DURANTE SONIC BOOM ==========
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isUsingSonicBoom()) {
            this.getNavigation().stop();
            super.travel(new Vec3(0, travelVector.y, 0));
            return;
        }
        super.travel(travelVector);
    }

    // ========== MUERTE ==========

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        // Usar rotación vanilla, sin animación personalizada
    }

    // ========== TICK PRINCIPAL ==========

    @Override
    public void tick() {
        super.tick();

        // Cooldown de ataque
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        // Cooldown de sonic boom
        int sonicCooldown = this.getPersistentData().getInt("sonicCooldown");
        if (sonicCooldown > 0) {
            this.getPersistentData().putInt("sonicCooldown", sonicCooldown - 1);
        }

        // ANIMACIÓN DE ATAQUE NORMAL - Estilo melee
        if (this.attackAnimTicks > 0) {
            this.attackAnimTicks--;

            // Ejecutar daño en el MEDIO de la animación (tick 10 de 20)
            if (this.attackAnimTicks == 10 && this.attackTarget != null) {
                this.performNormalAttack();
            }

            // Al terminar la animación, limpiar target y estado
            if (this.attackAnimTicks <= 0) {
                this.attackTarget = null;
                this.entityData.set(ATTACKING, false);
            }
        }

        // ANIMACIÓN DE SONIC BOOM - Estilo melee
        if (this.sonicBoomAnimTicks > 0) {
            this.sonicBoomAnimTicks--;

            // Ejecutar sonic boom a MITAD de la animación (tick 24 de 47)
            if (this.sonicBoomAnimTicks == 24) {
                this.executeSonicBoom();
            }

            // Establecer cooldowns DESPUÉS de terminar la animación
            if (this.sonicBoomAnimTicks <= 0) {
                this.attackCooldown = 60; // 3 segundos de cooldown normal después de sonic boom
                this.getPersistentData().putInt("sonicCooldown", 300); // 15 segundos para el próximo sonic boom

                // ⚡ DESACTIVAR ESTADO DE SONIC BOOM
                this.entityData.set(USING_SONIC_BOOM, false);
            }
        }

        // Limpiar enemigos muertos del mapa
        this.enemyAttackCount.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            AABB searchArea = this.getBoundingBox().inflate(20);
            List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, searchArea);
            return nearbyEntities.stream().noneMatch(e -> e.getUUID().equals(uuid) && e.isAlive());
        });
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // El golem no retrocede
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {
        // Solo incrementar deathScore una vez
        if (this.deathScore == 0) {
            ++this.deathScore;

            // Ejecutar explosión inmediatamente al morir
            if (this.level() instanceof ServerLevel) {
                SculkGolemDeathExplosionProcedure.execute(
                        this.level(),
                        this,
                        this.getX(),
                        this.getY(),
                        this.getZ()
                );
            }
        }
    }

    // ========== ATRIBUTOS ==========

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 190.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_KNOCKBACK, 3.0);
    }

    // ========== CONTROLADOR DE ANIMACIÓN ==========

    private PlayState mainAnimationController(AnimationState<SculkGolemEntity> event) {
        // Movimiento normal
        if (event.isMoving()) {
            return event.setAndContinue(WALK);
        }

        return event.setAndContinue(IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "main", 5, this::mainAnimationController)
                        .triggerableAnim("attack", ATTACK)         // Ataque normal
                        .triggerableAnim("sonic_boom", SONIC_BOOM) // Sonic boom
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
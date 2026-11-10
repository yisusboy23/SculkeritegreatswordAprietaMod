package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PlayMessages;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemHurtByTargetGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemMeleeGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemSonicBoomGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemTargetPillagerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.SculkGolemTargetSlimeGoal;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.procedures.AttackCheckControlProcedure;
import net.tucas.sculkeritegreatsword.procedures.SculkGolemDeathExplosionProcedure;
import net.tucas.sculkeritegreatsword.procedures.SculkeritegreatswordkineticenergydashEffectExpiresProcedure;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SculkGolemEntity extends BaseGolemEntity {

    /** Clave usada por SculkGolemSonicBoomGoal. */
    public static final String SONIC_COOLDOWN_KEY = "sonicCooldown";

    private static final RawAnimation SONIC_BOOM = RawAnimation.begin().thenPlay("attack_sonic_boom");

    // ATTACKING viene de BaseGolemEntity (SculkGolemEntity.ATTACKING sigue funcionando por herencia)
    public static final EntityDataAccessor<Boolean> USING_SONIC_BOOM =
            SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(SculkGolemEntity.class, EntityDataSerializers.STRING);

    // ---------- Ataque normal ----------
    private static final int ATTACK_DURATION = 20;       // 1 segundo
    private static final int ATTACK_HIT_TICK = 10;       // golpe a mitad de animación
    private static final int ATTACK_COOLDOWN = 40;

    // ---------- Sonic boom ----------
    private static final int SONIC_BOOM_DURATION = 47;   // ≈ 2.33 s
    private static final int SONIC_BOOM_HIT_TICK = 24;   // a mitad de animación
    private static final int SONIC_BOOM_COOLDOWN = 300;  // 15 s
    private static final int COOLDOWN_AFTER_SONIC_BOOM = 60;

    private int attackCooldown = 0;

    // Conteo de ataques por enemigo (lo consultan los goals)
    private final Map<UUID, Integer> enemyAttackCount = new HashMap<>();

    public SculkGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.SCULK_GOLEM.get(), world);
    }

    public SculkGolemEntity(EntityType<SculkGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.getPersistentData().putBoolean("attack_hostiles", true);
        this.getPersistentData().putBoolean("defense", false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Daño 16: es el daño real que hacía antes (hurt directo). Ahora el golpe usa el atributo.
        return createGolemAttributes(190.0, 0.25, 4.0, 16.0, 32.0, 0.0)
                .add(Attributes.ATTACK_KNOCKBACK, 3.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(USING_SONIC_BOOM, false);
        this.entityData.define(TEXTURE, "sculk_golem");
    }

    // ---------- Textura ----------
    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(2, new SculkGolemSonicBoomGoal(this, 20.0, 300));
        // Prioridad 4 (después de seguir al dueño), igual que antes
        this.goalSelector.addGoal(4, new SculkGolemMeleeGoal(this, 1.0, true));
    }

    @Override
    protected void registerHurtByTargetGoal() {
        this.targetSelector.addGoal(3, new SculkGolemHurtByTargetGoal(this));
    }

    @Override
    protected boolean targetsHostileMobs() {
        return false; // usa su propio target con filtro (attack_hostiles / aldeanos)
    }

    @Override
    protected void registerExtraTargetGoals() {
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                entity -> AttackCheckControlProcedure.execute(this)
                        && entity instanceof Monster
                        && !entity.getType().toString().toLowerCase().contains("villager")));
        this.targetSelector.addGoal(5, new SculkGolemTargetPillagerGoal(this));
        this.targetSelector.addGoal(6, new SculkGolemTargetSlimeGoal(this));
    }

    // ---------- Items ----------
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SCULK);
    }

    /** Usar un huevo de spawn sobre el golem crea otro golem (comportamiento original). */
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
        SculkGolemEntity baby = ModEntities.SCULK_GOLEM.get().create(serverWorld);
        if (baby != null) {
            baby.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(baby.blockPosition()),
                    MobSpawnType.BREEDING, null, null);
        }
        return baby;
    }

    // ---------- Ataque normal (llamado por SculkGolemMeleeGoal) ----------
    public boolean canAttack() {
        return this.attackCooldown <= 0 && !this.isActionAnimating();
    }

    public void startAttackSequence(LivingEntity target) {
        if (this.canAttack()) {
            this.attackCooldown = ATTACK_COOLDOWN;
            this.startMeleeAction(target);
        }
    }

    @Override
    protected int getMeleeDuration() {
        return ATTACK_DURATION;
    }

    @Override
    protected int getMeleeHitTick() {
        return ATTACK_HIT_TICK;
    }

    /** Antes no había chequeo de distancia al golpear; el alcance lo valida el goal. */
    @Override
    protected double getMeleeReach() {
        return Double.MAX_VALUE;
    }

    @Override
    protected void onMeleeHit(LivingEntity target) {
        this.enemyAttackCount.merge(target.getUUID(), 1, Integer::sum);
    }

    public int getAttackCountForEnemy(UUID enemyUUID) {
        return this.enemyAttackCount.getOrDefault(enemyUUID, 0);
    }

    // ---------- Sonic boom (llamado por SculkGolemSonicBoomGoal) ----------
    public boolean isUsingSonicBoom() {
        return this.entityData.get(USING_SONIC_BOOM);
    }

    private void setUsingSonicBoom(boolean using) {
        this.entityData.set(USING_SONIC_BOOM, using);
    }

    public boolean canUseSonicBoom() {
        return this.getPersistentData().getInt(SONIC_COOLDOWN_KEY) <= 0 && !this.isActionAnimating();
    }

    public void startSonicBoomSequence() {
        if (this.isActionAnimating()) {
            return;
        }
        this.startAction("sonic_boom", SONIC_BOOM_DURATION);
        this.setUsingSonicBoom(true);
    }

    private void executeSonicBoom() {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        SculkeritegreatswordkineticenergydashEffectExpiresProcedure.execute(
                this.level(), this.getX(), this.getY(), this.getZ(), this);
        this.enemyAttackCount.clear();
    }

    @Override
    protected void onActionTick(int remainingTicks) {
        if (!this.isUsingSonicBoom()) {
            return;
        }
        if (remainingTicks == SONIC_BOOM_HIT_TICK) {
            this.executeSonicBoom();
        }
        if (remainingTicks <= 0) {
            this.attackCooldown = COOLDOWN_AFTER_SONIC_BOOM;
            this.getPersistentData().putInt(SONIC_COOLDOWN_KEY, SONIC_BOOM_COOLDOWN);
            this.setUsingSonicBoom(false);
        }
    }

    /** Quieto mientras lanza el sonic boom. */
    @Override
    protected boolean shouldFreezeMovement() {
        return this.isUsingSonicBoom();
    }

    // ---------- Tick ----------
    @Override
    protected void serverTick() {
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        this.tickPersistentCooldown(SONIC_COOLDOWN_KEY);

        // Limpieza de enemigos muertos/lejanos: 1 consulta por segundo (antes: 1 consulta por enemigo por tick)
        if (this.tickCount % 20 == 0 && !this.enemyAttackCount.isEmpty()) {
            AABB area = this.getBoundingBox().inflate(20);
            Set<UUID> alive = new HashSet<>();
            List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity e : nearby) {
                if (e.isAlive()) {
                    alive.add(e.getUUID());
                }
            }
            this.enemyAttackCount.keySet().removeIf(id -> !alive.contains(id));
        }
    }

    // ---------- Daño / físicas ----------
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurt(source, amount);
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
        // Explosión de muerte una sola vez
        if (this.deathScore == 0) {
            ++this.deathScore;
            if (this.level() instanceof ServerLevel) {
                SculkGolemDeathExplosionProcedure.execute(
                        this.level(), this, this.getX(), this.getY(), this.getZ());
            }
        }
    }

    // ---------- Sonidos (genéricos, como antes) ----------
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    // ---------- NBT ----------
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Texture", this.getTexture());
        compound.putBoolean("attack_hostiles", this.getPersistentData().getBoolean("attack_hostiles"));
        compound.putBoolean("defense", this.getPersistentData().getBoolean("defense"));
        compound.putInt("AttackCooldown", this.attackCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Texture")) {
            this.setTexture(compound.getString("Texture"));
        }
        this.getPersistentData().putBoolean("attack_hostiles",
                !compound.contains("attack_hostiles") || compound.getBoolean("attack_hostiles"));
        this.getPersistentData().putBoolean("defense",
                compound.contains("defense") && compound.getBoolean("defense"));
        this.attackCooldown = compound.getInt("AttackCooldown");
    }

    // ---------- Animación ----------
    @Override
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {
        controller.triggerableAnim("sonic_boom", SONIC_BOOM);
    }
}
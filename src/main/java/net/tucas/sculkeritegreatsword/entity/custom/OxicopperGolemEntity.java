package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.ai.goal.OxicopperGolemFleeFromEnemyGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.OxicopperGolemShellAttackGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.OxicopperGolemThunderGoal;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OxicopperGolemEntity extends BaseGolemEntity {

    // ========== ANIMACIONES ==========
    private static final RawAnimation SHELL_IDLE = RawAnimation.begin().thenLoop("shell_idle");
    private static final RawAnimation FOLD = RawAnimation.begin().thenPlay("fold");
    private static final RawAnimation UNFOLD = RawAnimation.begin().thenPlay("unfold");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation THUNDER = RawAnimation.begin().thenPlay("shot_thunder");

    // ========== DATOS SINCRONIZADOS ==========
    private static final EntityDataAccessor<Boolean> IN_SHELL =
            SynchedEntityData.defineId(OxicopperGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EMISSIVE =
            SynchedEntityData.defineId(OxicopperGolemEntity.class, EntityDataSerializers.BOOLEAN);

    // ========== DISPARO NORMAL ==========
    private int shootAnimTicks = 0;
    private LivingEntity shootTarget = null;
    private int shootCooldown = 0;

    // ========== GOLPES RECIBIDOS ==========
    private static final int HIT_RESET_TIME = 60;
    private static final int HITS_TO_FORCE_MOVE = 4;
    private int consecutiveHits = 0;
    private int hitResetTimer = 0;

    // ========== TRUENOS ==========
    private static final int THUNDER_COOLDOWN = 300; // 15 segundos
    private static final int THUNDER_DURATION = 40;  // 2 segundos
    private static final int[] THUNDER_STRIKE_TICKS = {15, 25, 35};
    private int thunderCooldown = 0;
    private int thunderAnimTicks = 0;
    private List<LivingEntity> thunderTargets = new ArrayList<>();
    private boolean thunderInvulnerable = false;
    private int emissiveFlickerTimer = 0;
    private int nextStrikeIndex = 0;

    public OxicopperGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(60.0, 0.28, 6.0, 8.0, 32.0, 0.9);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IN_SHELL, false);
        this.entityData.define(IS_EMISSIVE, false);
    }

    // ========== GETTERS / SETTERS ==========
    public boolean isInShell() {
        return this.entityData.get(IN_SHELL);
    }

    private void setInShell(boolean inShell) {
        this.entityData.set(IN_SHELL, inShell);
    }

    /** Público: lo usan el Renderer y el Model. */
    public boolean isEmissive() {
        return this.entityData.get(IS_EMISSIVE);
    }

    private void setEmissive(boolean emissive) {
        this.entityData.set(IS_EMISSIVE, emissive);
    }

    // ========== GOALS ==========
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(0, new OxicopperGolemThunderGoal(this, 20.0, 3));
        this.goalSelector.addGoal(1, new OxicopperGolemFleeFromEnemyGoal(this, 1.2, 5.0f));
        this.goalSelector.addGoal(2, new OxicopperGolemShellAttackGoal(this, 16.0, 6.0));
    }

    @Override
    protected void registerExtraTargetGoals() {
        // Slime no es un Monster; Pillager sí lo es, ya lo cubre el target estándar
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Slime.class, true));
    }

    // ========== ITEMS (cobre: domesticar y curar) ==========
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COPPER_INGOT);
    }

    // ========== DAÑO ==========
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Invulnerable durante el ataque de trueno
        if (this.thunderInvulnerable) {
            return false;
        }
        // Inmune a rayos (incluidos los suyos) y a ahogarse
        if (source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypes.DROWN)) {
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

    // ========== NBT (solo cooldowns y concha; los estados temporales NO se guardan) ==========
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("InShell", this.isInShell());
        compound.putInt("ShootCooldown", this.shootCooldown);
        compound.putInt("ThunderCooldown", this.thunderCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setInShell(compound.getBoolean("InShell"));
        this.shootCooldown = compound.getInt("ShootCooldown");
        this.thunderCooldown = compound.getInt("ThunderCooldown");
    }

    // ========== TICK ==========
    @Override
    protected void serverTick() {
        if (this.shootCooldown > 0) {
            this.shootCooldown--;
        }
        if (this.thunderCooldown > 0) {
            this.thunderCooldown--;
        }

        if (this.hitResetTimer > 0) {
            this.hitResetTimer--;
            if (this.hitResetTimer <= 0) {
                this.consecutiveHits = 0;
            }
        }

        // Disparo normal
        if (this.shootAnimTicks > 0) {
            this.shootAnimTicks--;
            if (this.shootAnimTicks == 11 && this.shootTarget != null && this.shootTarget.isAlive()) {
                this.executeShoot();
            }
            if (this.shootAnimTicks <= 0) {
                this.shootTarget = null;
            }
        }

        // Trueno
        if (this.thunderAnimTicks > 0) {
            this.thunderAnimTicks--;

            // Parpadeo de la textura emisiva (cada 2 ticks)
            this.emissiveFlickerTimer++;
            if (this.emissiveFlickerTimer % 2 == 0) {
                this.setEmissive(!this.isEmissive());
            }

            if (this.nextStrikeIndex < THUNDER_STRIKE_TICKS.length) {
                int elapsed = THUNDER_DURATION - this.thunderAnimTicks;
                if (elapsed == THUNDER_STRIKE_TICKS[this.nextStrikeIndex]) {
                    this.executeThunderStrike(this.nextStrikeIndex);
                    this.nextStrikeIndex++;
                }
            }

            if (this.thunderAnimTicks <= 0) {
                this.thunderTargets.clear();
                this.thunderInvulnerable = false;
                this.setEmissive(false);
                this.emissiveFlickerTimer = 0;
                this.nextStrikeIndex = 0;
            }
        }
    }

    @Override
    protected boolean shouldFreezeMovement() {
        return this.isInShell() || this.thunderInvulnerable;
    }

    // ========== CONCHA ==========
    public void enterShell() {
        if (!this.isInShell()) {
            this.setInShell(true);
            this.triggerAnim(CONTROLLER_MAIN, "fold");
        }
    }

    public void exitShell() {
        if (this.isInShell()) {
            this.forceExitShell();
        }
    }

    private void forceExitShell() {
        this.setInShell(false);
        this.consecutiveHits = 0;
        this.hitResetTimer = 0;
        this.triggerAnim(CONTROLLER_MAIN, "unfold");
    }

    // ========== DISPARO NORMAL ==========
    public void startShootSequence(LivingEntity target) {
        if (this.shootCooldown <= 0 && !this.isShooting()) {
            this.shootTarget = target;
            this.shootAnimTicks = 22;
            this.shootCooldown = 25;
            this.triggerAnim(CONTROLLER_MAIN, "shoot");
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
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t,
                    start.z + (end.z - start.z) * t,
                    2, 0.1, 0.1, 0.1, 0.02);
        }

        this.shootTarget.hurt(this.damageSources().mobAttack(this), 10.0f);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, this.getSoundSource(), 0.5f, 1.5f);
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

    public int getConsecutiveHits() {
        return this.consecutiveHits;
    }

    public void resetConsecutiveHits() {
        this.consecutiveHits = 0;
    }

    // ========== TRUENO (llamado por OxicopperGolemThunderGoal) ==========
    public boolean canUseThunder() {
        return this.thunderCooldown <= 0 && this.thunderAnimTicks <= 0 && !this.isUsingThunder();
    }

    public boolean isUsingThunder() {
        return this.thunderAnimTicks > 0;
    }

    public List<LivingEntity> getThunderTargets() {
        return this.thunderTargets;
    }

    public void startThunderAttack(List<LivingEntity> targets) {
        if (targets.isEmpty() || !this.canUseThunder()) {
            return;
        }

        this.thunderTargets = new ArrayList<>(targets);
        this.thunderAnimTicks = THUNDER_DURATION;
        this.thunderCooldown = THUNDER_COOLDOWN;
        this.thunderInvulnerable = true;
        this.emissiveFlickerTimer = 0;
        this.nextStrikeIndex = 0;

        this.triggerAnim(CONTROLLER_MAIN, "thunder");
    }

    private void executeThunderStrike(int targetIndex) {
        if (!(this.level() instanceof ServerLevel serverLevel) || targetIndex >= this.thunderTargets.size()) {
            return;
        }

        LivingEntity target = this.thunderTargets.get(targetIndex);
        if (target == null || !target.isAlive()) {
            return;
        }

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(target.getX(), target.getY(), target.getZ());
            serverLevel.addFreshEntity(lightning);
        }

        Vec3 start = this.position().add(0, this.getBbHeight() * 0.5, 0);
        Vec3 end = target.position().add(0, target.getBbHeight() * 0.5, 0);

        for (int i = 0; i < 30; i++) {
            double t = i / 30.0;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t,
                    start.z + (end.z - start.z) * t,
                    5, 0.2, 0.2, 0.2, 0.05);
        }

        target.hurt(this.damageSources().lightningBolt(), 15.0f);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, this.getSoundSource(), 1.5f, 1.0f + (targetIndex * 0.1f));
    }

    // ========== ANIMACIÓN ==========
    @Nullable
    @Override
    protected RawAnimation getStateAnimation(AnimationState<BaseGolemEntity> state) {
        return this.isInShell() ? SHELL_IDLE : null;
    }

    @Override
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {
        controller.triggerableAnim("shoot", SHOOT);
        controller.triggerableAnim("fold", FOLD);
        controller.triggerableAnim("unfold", UNFOLD);
        controller.triggerableAnim("thunder", THUNDER);
    }
}
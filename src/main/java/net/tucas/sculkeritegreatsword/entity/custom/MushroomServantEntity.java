package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomServantExplodeGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomServantTargetHostilesGoal;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.List;

public class MushroomServantEntity extends BaseGolemEntity {

    private static final RawAnimation ANIM_SPAWN = RawAnimation.begin().thenPlay("spawn");
    private static final RawAnimation ANIM_EXPLOSION = RawAnimation.begin().thenLoop("explotion");

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MushroomServantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPLOSION_STATE =
            SynchedEntityData.defineId(MushroomServantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_SPAWNED =
            SynchedEntityData.defineId(MushroomServantEntity.class, EntityDataSerializers.BOOLEAN);

    // TODO: ajusta a la duración real (en ticks) de tu animación "spawn". Antes era 1 tick.
    private static final int SPAWN_ANIMATION_TICKS = 1;

    // Desaparece si no encuentra enemigos en 30 segundos
    private static final int MAX_IDLE_TIME = 600;
    private int idleTimer = 0;

    public MushroomServantEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
        this.xpReward = 5;
        // Flags que leen los goals SculkGolem*. Si ya no los necesitas, bórralos.
        this.getPersistentData().putBoolean("attack_hostiles", true);
        this.getPersistentData().putBoolean("defense", false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(20.0, 0.25, 0.0, 3.0, 15.0, 0.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, this.random.nextInt(2));
        this.entityData.define(EXPLOSION_STATE, 0);
        this.entityData.define(HAS_SPAWNED, false);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(0, new MushroomServantExplodeGoal(this));
    }

    @Override
    protected boolean targetsHostileMobs() {
        return false; // usa su propio goal
    }

    @Override
    protected void registerExtraTargetGoals() {
        this.targetSelector.addGoal(4, new MushroomServantTargetHostilesGoal(this));
    }

    // ---------- Tick ----------
    @Override
    protected void serverTick() {
        // Animación de spawn solo una vez
        if (!this.hasSpawned() && this.tickCount >= SPAWN_ANIMATION_TICKS) {
            this.setHasSpawned(true);
        }

        // Para que la animación de correr funcione en el cliente (getTarget() solo existe en el servidor)
        this.setAggressive(this.getTarget() != null);

        // Desaparecer tras 30 s sin enemigos
        if (this.isTame()) {
            if (this.getTarget() == null) {
                this.idleTimer++;
                if (this.idleTimer >= MAX_IDLE_TIME) {
                    this.disappearWithSmoke();
                }
            } else {
                this.idleTimer = 0;
            }
        }
    }

    private void disappearWithSmoke() {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + this.random.nextDouble(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        1, 0.0, 0.1, 0.0, 0.0);
            }
            this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.0F);
            this.discard();
        }
    }

    // ---------- Explosión ----------
    public void explode() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);

        for (int i = 0; i < 30; i++) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getY() + (this.random.nextDouble() - 0.5) * 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                    1, 0.0, 0.1, 0.0, 0.02);
        }

        this.playSound(SoundEvents.GENERIC_EXPLODE, 4.0F, 1.0F);

        AABB area = new AABB(
                this.getX() - 3.5, this.getY() - 3.5, this.getZ() - 3.5,
                this.getX() + 3.5, this.getY() + 3.5, this.getZ() + 3.5);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            // NO dañar: a sí mismo, a jugadores, ni a NINGUNA mascota (TamableAnimal cubre
            // a todos los golems y a otros servants)
            if (entity == this || entity instanceof Player || entity instanceof TamableAnimal) {
                continue;
            }
            double distance = this.distanceTo(entity);
            if (distance <= 3.5) {
                float damage = (float) (40.0 * (1.0 - (distance / 3.5)));
                entity.hurt(this.damageSources().explosion(this, this), damage);
            }
        }

        this.discard();
    }

    // ---------- Getters / setters ----------
    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public int getExplosionState() {
        return this.entityData.get(EXPLOSION_STATE);
    }

    public void setExplosionState(int state) {
        this.entityData.set(EXPLOSION_STATE, state);
    }

    public boolean hasSpawned() {
        return this.entityData.get(HAS_SPAWNED);
    }

    public void setHasSpawned(boolean spawned) {
        this.entityData.set(HAS_SPAWNED, spawned);
    }

    // ---------- NBT ----------
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant());
        tag.putBoolean("HasSpawned", this.hasSpawned());
        tag.putInt("IdleTimer", this.idleTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        }
        if (tag.contains("HasSpawned")) {
            this.setHasSpawned(tag.getBoolean("HasSpawned"));
        }
        if (tag.contains("IdleTimer")) {
            this.idleTimer = tag.getInt("IdleTimer");
        }
    }

    // ---------- Animación ----------
    @Nullable
    @Override
    protected RawAnimation getStateAnimation(AnimationState<BaseGolemEntity> state) {
        if (!this.hasSpawned()) {
            return ANIM_SPAWN;
        }
        if (this.getExplosionState() > 0) {
            return ANIM_EXPLOSION;
        }
        if (state.isMoving() && this.isAggressive()) {
            return ANIM_RUN;
        }
        return null; // walk / idle estándar
    }
}
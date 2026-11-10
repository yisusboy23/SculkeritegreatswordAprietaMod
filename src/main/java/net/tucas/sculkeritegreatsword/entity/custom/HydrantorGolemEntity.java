package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.tucas.sculkeritegreatsword.entity.ai.goal.HydrantorWindAttackGoal;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class HydrantorGolemEntity extends BaseGolemEntity {

    /** Clave usada por HydrantorWindAttackGoal. */
    public static final String WIND_COOLDOWN_KEY = "windAttackCooldown";

    private static final RawAnimation ATTACK_WIND = RawAnimation.begin().thenPlay("attack_wind");

    public static final EntityDataAccessor<Boolean> USING_WIND_ATTACK =
            SynchedEntityData.defineId(HydrantorGolemEntity.class, EntityDataSerializers.BOOLEAN);

    // El golpe lanza al enemigo 2 ticks después del daño
    private static final int LAUNCH_TICK = 16;

    public HydrantorGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.HYDRANTOR_GOLEM.get(), world);
    }

    public HydrantorGolemEntity(EntityType<HydrantorGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // KNOCKBACK_RESISTANCE: el máximo real es 1.0 (antes tenía 2.8, se recortaba igual)
        return createGolemAttributes(180.0, 0.25, 16.0, 20.0, 20.0, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(USING_WIND_ATTACK, false);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new HydrantorWindAttackGoal(this, 12.0, 300));
    }

    // ---------- Melee (30 ticks, daño cuando quedan 18, lanzamiento cuando quedan 16) ----------
    @Override
    protected boolean hasMeleeAnimation() {
        return true;
    }

    @Override
    protected int getMeleeDuration() {
        return 30;
    }

    @Override
    protected int getMeleeHitTick() {
        return 18;
    }

    @Override
    protected void onActionTick(int remainingTicks) {
        if (remainingTicks != LAUNCH_TICK) {
            return;
        }
        LivingEntity target = this.getMeleeTarget();
        if (target == null || !target.isAlive() || this.distanceTo(target) >= this.getMeleeReach()) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length > 0) {
            dx /= length;
            dz /= length;
        }
        target.setDeltaMovement(dx * 0.4, 0.78, dz * 0.4); // ≈ 7 bloques de altura
        target.hasImpulse = true;
        target.hurtMarked = true; // sincroniza el impulso con jugadores
    }

    // ---------- Items ----------
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.IRON_INGOT);
    }

    // ---------- Wind attack ----------
    public boolean isUsingWindAttack() {
        return this.entityData.get(USING_WIND_ATTACK);
    }

    public void setUsingWindAttack(boolean using) {
        this.entityData.set(USING_WIND_ATTACK, using);
    }

    // ---------- Tick ----------
    @Override
    protected void serverTick() {
        this.tickPersistentCooldown(WIND_COOLDOWN_KEY);
    }

    // ---------- Animación ----------
    @Override
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {
        controller.triggerableAnim("attack_wind", ATTACK_WIND);
    }
}
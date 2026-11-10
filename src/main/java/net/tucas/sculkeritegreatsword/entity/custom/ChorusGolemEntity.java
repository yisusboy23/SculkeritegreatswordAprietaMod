package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.tucas.sculkeritegreatsword.entity.ai.goal.ChorusGolemTeleportAttackGoal;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class ChorusGolemEntity extends BaseGolemEntity {

    /** Clave usada por ChorusGolemTeleportAttackGoal. */
    public static final String TELEPORT_COOLDOWN_KEY = "teleportCooldown";

    private static final RawAnimation TELEPORT_ATTACK = RawAnimation.begin().thenPlay("teleport_attack");

    public ChorusGolemEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.CHORUS_GOLEM.get(), world);
    }

    public ChorusGolemEntity(EntityType<ChorusGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(200.0, 0.3, 4.0, 20.0, 16.0, 1.0);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new ChorusGolemTeleportAttackGoal(this, 8.0, 200)); // 8 bloques, 10 s cooldown
    }

    // ---------- Melee (20 ticks, golpe cuando quedan 10) ----------
    @Override
    protected boolean hasMeleeAnimation() {
        return true;
    }

    @Override
    protected int getMeleeDuration() {
        return 20;
    }

    @Override
    protected int getMeleeHitTick() {
        return 10;
    }

    // ---------- Items ----------
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CHORUS_FRUIT);
    }

    // ---------- Daño ----------
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Inmune a proyectiles (flechas, bolas de fuego, tridents, wither skulls...)
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    // ---------- Tick ----------
    @Override
    protected void serverTick() {
        this.tickPersistentCooldown(TELEPORT_COOLDOWN_KEY);
    }

    // ---------- Animación ----------
    @Override
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {
        controller.triggerableAnim("teleport_attack", TELEPORT_ATTACK);
    }
}
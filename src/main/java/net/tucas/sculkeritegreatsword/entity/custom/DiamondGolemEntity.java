package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.entity.ai.goal.DiamondGolemProyectileAttackGoal;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class DiamondGolemEntity extends BaseGolemEntity {

    private static final RawAnimation DIAMOND_ATTACK = RawAnimation.begin().thenPlay("diamond_attack");
    private static final int PROJECTILE_ANIMATION_DURATION = 40; // 2 segundos

    public DiamondGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.5f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(150.0, 0.25, 8.0, 18.0, 48.0, 0.8);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new DiamondGolemProyectileAttackGoal(this, 16.0, 8.0));
    }

    // ---------- Melee (30 ticks, golpe cuando quedan 15) ----------
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
        return 15;
    }

    // ---------- Proyectil (llamado desde DiamondGolemProyectileAttackGoal) ----------
    public void performProjectileAttack() {
        this.startAction("diamond_attack", PROJECTILE_ANIMATION_DURATION);
    }

    // ---------- Sonidos ----------
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GLASS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    // ---------- Animación ----------
    @Override
    protected void registerTriggerableAnims(AnimationController<BaseGolemEntity> controller) {
        controller.triggerableAnim("diamond_attack", DIAMOND_ATTACK);
    }
}
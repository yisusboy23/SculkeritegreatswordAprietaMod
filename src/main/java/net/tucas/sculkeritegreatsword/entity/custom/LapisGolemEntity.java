package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.entity.ai.goal.LapisGolemCombatGoal;
import net.tucas.sculkeritegreatsword.init.ModParticles;

public class LapisGolemEntity extends BaseGolemEntity {

    private static final int ATTACK_ANIMATION_LENGTH = 30; // 1.5 segundos

    public LapisGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(40.0, 0.23, 12.0, 0.0, 48.0, 0.8);
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new LapisGolemCombatGoal(this));
    }

    @Override
    protected boolean targetsHostileMobs() {
        return false; // su LapisGolemCombatGoal maneja el combate
    }

    // ---------- Curación con lapislázuli ----------
    @Override
    protected boolean isHealItem(ItemStack stack, Player player) {
        return stack.is(Items.LAPIS_LAZULI);
    }

    @Override
    protected float getHealAmount() {
        return 5.0f;
    }

    @Override
    protected void onHealed(ServerLevel level) {
        super.onHealed(level); // corazones
        for (int i = 0; i < 10; i++) {
            level.sendParticles(ModParticles.LAPIS_CONTROL.get(),
                    this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                    1, 0, 0.1, 0, 0.02);
        }
        level.playSound(null, this.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, this.getSoundSource(), 1.0F, 1.0F);
    }

    // ---------- Ataque (llamado por LapisGolemCombatGoal) ----------
    public void startAttackAnimation() {
        this.startAction("attack", ATTACK_ANIMATION_LENGTH);
    }

    @Override
    protected void onActionTick(int remainingTicks) {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.spawnAttackParticles(serverLevel);
        }
    }

    private void spawnAttackParticles(ServerLevel level) {
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5;
            double offsetY = this.random.nextDouble() * this.getBbHeight();
            double offsetZ = (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.5;

            level.sendParticles(ModParticles.LAPIS_CONTROL.get(),
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    1, 0, 0.05, 0, 0.03);
        }
    }

    public float getHealthPercentage() {
        return this.getHealth() / this.getMaxHealth();
    }

    // ---------- Sonidos ----------
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }
}
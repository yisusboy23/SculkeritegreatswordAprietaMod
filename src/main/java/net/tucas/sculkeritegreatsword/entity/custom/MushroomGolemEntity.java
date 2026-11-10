package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomGolemAuraGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomGolemFleeAndHealGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomGolemGasAttackGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.MushroomGolemTauntGoal;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.List;

public class MushroomGolemEntity extends BaseGolemEntity {

    private static final RawAnimation ANIM_GAS = RawAnimation.begin().thenLoop("attack");

    public enum Variant {
        RED,
        BROWN
    }

    private static final EntityDataAccessor<Boolean> IS_RETREATING =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GAS_RELEASING =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TAUNTING =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> STORED_POTION =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MushroomGolemEntity.class, EntityDataSerializers.INT);

    private MobEffectInstance storedEffect = null;

    public MushroomGolemEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createGolemAttributes(70.0, 0.25, 14.0, 0.0, 48.0, 0.9);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_RETREATING, false);
        this.entityData.define(IS_GAS_RELEASING, false);
        this.entityData.define(IS_TAUNTING, true);
        this.entityData.define(STORED_POTION, ItemStack.EMPTY);
        this.entityData.define(VARIANT, Variant.RED.ordinal());
    }

    // ---------- Goals ----------
    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(0, new MushroomGolemFleeAndHealGoal(this));
        this.goalSelector.addGoal(1, new MushroomGolemGasAttackGoal(this));
        this.goalSelector.addGoal(2, new MushroomGolemAuraGoal(this));
        this.goalSelector.addGoal(3, new MushroomGolemTauntGoal(this));
    }

    @Override
    protected boolean targetsHostileMobs() {
        return false; // su comportamiento es de soporte (aura, gas, taunt)
    }

    // ---------- Domesticar / curar ----------
    @Override
    protected boolean isTameItem(ItemStack stack) {
        return stack.is(Items.RED_MUSHROOM) || stack.is(Items.BROWN_MUSHROOM);
    }

    /** Shift + bone meal */
    @Override
    protected boolean isHealItem(ItemStack stack, Player player) {
        return player.isShiftKeyDown() && stack.is(Items.BONE_MEAL);
    }

    @Override
    protected float getHealAmount() {
        return 25.0f;
    }

    @Override
    protected void onHealed(ServerLevel level) {
        super.onHealed(level); // corazones
        level.playSound(null, this.blockPosition(),
                SoundEvents.BONE_MEAL_USE, this.getSoundSource(), 1.0F, 1.0F);
    }

    // ---------- Almacenar / extraer poción (shift) ----------
    @Nullable
    @Override
    protected InteractionResult onOwnerInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.isShiftKeyDown()) {
            return null;
        }
        boolean clientSide = this.level().isClientSide;

        if (stack.is(Items.POTION) && this.getStoredPotion().isEmpty()) {
            if (!clientSide) {
                ItemStack stored = stack.copy();
                stored.setCount(1);
                this.setStoredPotion(stored);
                this.extractPotionEffect(stored);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.giveToPlayer(player, new ItemStack(Items.GLASS_BOTTLE));
            }
            return InteractionResult.sidedSuccess(clientSide);
        }

        if (stack.is(Items.GLASS_BOTTLE) && !this.getStoredPotion().isEmpty()) {
            if (!clientSide) {
                this.giveToPlayer(player, this.getStoredPotion().copy());
                this.setStoredPotion(ItemStack.EMPTY);
                this.storedEffect = null;
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(clientSide);
        }
        return null;
    }

    private void giveToPlayer(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false); // inventario lleno: no se pierde el item
        }
    }

    private void extractPotionEffect(ItemStack potionStack) {
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);
        if (!effects.isEmpty()) {
            this.storedEffect = effects.get(0);
        }
    }

    public int countNearbyEnemies(double radius) {
        AABB area = new AABB(this.blockPosition()).inflate(radius);
        List<Mob> enemies = this.level().getEntitiesOfClass(Mob.class, area);
        return (int) enemies.stream()
                .filter(mob -> mob instanceof Enemy && mob.isAlive())
                .count();
    }

    // ---------- Getters / setters ----------
    public Variant getVariant() {
        int index = Math.max(0, Math.min(this.entityData.get(VARIANT), Variant.values().length - 1));
        return Variant.values()[index];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(VARIANT, variant.ordinal());
    }

    public boolean isRetreating() {
        return this.entityData.get(IS_RETREATING);
    }

    public void setRetreating(boolean retreating) {
        this.entityData.set(IS_RETREATING, retreating);
    }

    public boolean isGasReleasing() {
        return this.entityData.get(IS_GAS_RELEASING);
    }

    public void setGasReleasing(boolean releasing) {
        this.entityData.set(IS_GAS_RELEASING, releasing);
    }

    public boolean isTaunting() {
        return this.entityData.get(IS_TAUNTING);
    }

    public void setTaunting(boolean taunting) {
        this.entityData.set(IS_TAUNTING, taunting);
    }

    public boolean hasActivePotion() {
        return this.storedEffect != null;
    }

    public MobEffectInstance getStoredEffect() {
        return this.storedEffect;
    }

    public ItemStack getStoredPotion() {
        return this.entityData.get(STORED_POTION);
    }

    public void setStoredPotion(ItemStack potion) {
        this.entityData.set(STORED_POTION, potion);
    }

    // ---------- NBT ----------
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", this.getVariant().ordinal());
        if (!this.getStoredPotion().isEmpty()) {
            tag.put("StoredPotion", this.getStoredPotion().save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            int index = Math.max(0, Math.min(tag.getInt("Variant"), Variant.values().length - 1));
            this.setVariant(Variant.values()[index]);
        }
        if (tag.contains("StoredPotion")) {
            this.setStoredPotion(ItemStack.of(tag.getCompound("StoredPotion")));
            this.extractPotionEffect(this.getStoredPotion());
        }
    }

    // ---------- Animación ----------
    @Nullable
    @Override
    protected RawAnimation getStateAnimation(AnimationState<BaseGolemEntity> state) {
        if (this.isRetreating()) {
            return ANIM_RUN;
        }
        if (this.isGasReleasing()) {
            return ANIM_GAS;
        }
        return null; // idle / walk estándar
    }
}
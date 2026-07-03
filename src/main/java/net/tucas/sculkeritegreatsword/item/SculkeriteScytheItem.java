package net.tucas.sculkeritegreatsword.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.tucas.sculkeritegreatsword.procedures.SculkeriteScytheRightclickedProcedure;

public class SculkeriteScytheItem extends SwordItem {
    public SculkeriteScytheItem() {
        super(Tiers.NETHERITE, 5, -2.2f, new Properties().durability(2875));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 1.0f;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        SculkeriteScytheRightclickedProcedure.execute(level, player, result.getObject());
        return result;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 11;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}
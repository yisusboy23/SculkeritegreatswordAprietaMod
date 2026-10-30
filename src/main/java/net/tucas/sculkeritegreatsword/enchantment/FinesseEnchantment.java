package net.tucas.sculkeritegreatsword.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class FinesseEnchantment extends Enchantment {
    public FinesseEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override public int getMaxLevel() { return 3; }
    @Override public int getMinCost(int level) { return 10 + (level - 1) * 10; }
    @Override public int getMaxCost(int level) { return getMinCost(level) + 30; }
    @Override public boolean canEnchant(ItemStack stack) { return stack.getItem() instanceof BrushItem; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return stack.getItem() instanceof BrushItem; }
}
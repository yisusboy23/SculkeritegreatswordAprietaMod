package net.tucas.sculkeritegreatsword.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.tucas.sculkeritegreatsword.item.Moditems;

public class KineticenergyEnchantment extends Enchantment {
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("kineticenergy", item ->
            item == Moditems.SCULKERITEGREATSWORD.get() || item == Moditems.SCULKERITE_SCYTHE.get()
    );

    public KineticenergyEnchantment() {
        super(Rarity.VERY_RARE, CATEGORY, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 11;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() == Moditems.SCULKERITEGREATSWORD.get() || stack.getItem() == Moditems.SCULKERITE_SCYTHE.get();
    }
}
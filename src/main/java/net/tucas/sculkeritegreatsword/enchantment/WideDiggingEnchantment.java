package net.tucas.sculkeritegreatsword.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantments;

public class WideDiggingEnchantment extends Enchantment {
    // EnchantmentCategory que solo permite picos y palas
    private static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("widedigging", item ->
            item instanceof PickaxeItem || item instanceof ShovelItem
    );

    public WideDiggingEnchantment() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2; // Nivel 1 = 2x2, Nivel 2 = 3x3
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return isToolValid(stack.getItem());
    }

    private static boolean isToolValid(Item item) {
        return item instanceof PickaxeItem || item instanceof ShovelItem;
    }
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // Es incompatible con Eficiencia
        if (other == Enchantments.BLOCK_EFFICIENCY) {
            return false;
        }
        return super.checkCompatibility(other);
    }

}
package net.tucas.sculkeritegreatsword.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.tucas.sculkeritegreatsword.init.ModEnchantments;

public final class EggscellentHelper {
    private EggscellentHelper() {}

    public static final TagKey<Item> TARGETS = TagKey.create(Registries.ITEM,
            new ResourceLocation("sculkeritegreatsword", "eggscellent_targets"));

    /** Repeticiones extra por nivel (I, II, III). */
    private static final int[] REROLLS = {2, 4, 6};

    public static int getLevel(Player player) {
        ItemStack brush = player.getUseItem();
        if (!(brush.getItem() instanceof BrushItem)) return 0;
        return Math.min(EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EGGSCELLENT.get(), brush), 3);
    }

    public static ItemStack improve(ServerLevel level, Player player, BlockPos pos,
                                    ResourceLocation tableId, ItemStack original) {
        int lvl = getLevel(player);
        if (lvl <= 0) return original;
        if (!original.isEmpty() && original.is(TARGETS)) return original;
        if (level.getServer() == null) return original;

        LootTable table = level.getServer().getLootData().getLootTable(tableId);
        if (table == LootTable.EMPTY) return original;

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withLuck(player.getLuck())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.CHEST);

        for (int i = 0; i < REROLLS[lvl - 1]; i++) {
            for (ItemStack candidate : table.getRandomItems(params)) {
                if (!candidate.isEmpty() && candidate.is(TARGETS)) return candidate;
            }
        }
        return original;
    }
}
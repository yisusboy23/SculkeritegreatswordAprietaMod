package net.tucas.sculkeritegreatsword.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.init.ModEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FinesseHelper {
    private FinesseHelper() {}

    public static int getFinesseLevel(Player player) {
        ItemStack brush = player.getUseItem();
        if (!(brush.getItem() instanceof BrushItem)) return 0;
        return Math.min(EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FINESSE.get(), brush), 3);
    }

    public static void rollExtraRewards(BrushableBlockEntity be, Player player,
                                        ResourceLocation tableId, @Nullable Direction hitDirection) {
        Level level = be.getLevel();
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.getServer() == null) return;

        int rolls = getFinesseLevel(player);
        if (rolls <= 0) return;

        LootTable table = serverLevel.getServer().getLootData().getLootTable(tableId);
        if (table == LootTable.EMPTY) return;

        BlockPos pos = be.getBlockPos();
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withLuck(player.getLuck())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.CHEST);

        for (int i = 0; i < rolls; i++) {
            if (serverLevel.random.nextFloat() >= 0.5F) continue;
            var items = table.getRandomItems(params);
            if (items.isEmpty()) continue;
            ItemStack reward = items.get(0);
            if (reward.isEmpty()) continue;
            spawnReward(serverLevel, pos, hitDirection, reward);
        }
    }

    private static void spawnReward(ServerLevel level, BlockPos pos,
                                    @Nullable Direction hitDirection, ItemStack stack) {
        double width = EntityType.ITEM.getWidth();
        double d1 = 1.0D - width;
        double d2 = width / 2.0D;
        Direction dir = Objects.requireNonNullElse(hitDirection, Direction.UP);
        BlockPos p = pos.relative(dir, 1);
        double x = p.getX() + 0.5D * d1 + d2;
        double y = p.getY() + 0.5D + (EntityType.ITEM.getHeight() / 2.0F);
        double z = p.getZ() + 0.5D * d1 + d2;

        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(entity);
    }
}
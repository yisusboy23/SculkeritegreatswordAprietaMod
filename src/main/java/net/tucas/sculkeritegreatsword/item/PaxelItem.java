package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;

public class PaxelItem extends DiggerItem {

    public PaxelItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super((float)attackDamage, attackSpeed, tier, BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
                state.is(BlockTags.MINEABLE_WITH_AXE) ||
                state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isCorrectToolForDrops(state) ? this.speed : 1.0F;
    }
}
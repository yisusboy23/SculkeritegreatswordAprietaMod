package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.tucas.sculkeritegreatsword.entity.custom.PeekerEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

public class PeekerFetusBlock extends Block {

    private static final int HATCH_CHANCE_ONE_IN = 5; // 1 de cada 30 random ticks

    public PeekerFetusBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(HATCH_CHANCE_ONE_IN) == 0) {
            hatch(level, pos);
        }
    }

    private void hatch(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
        PeekerEntity baby = ModEntities.PEEKER.get().create(level);
        if (baby != null) {
            baby.setAge(-24000); // nace bebé
            baby.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            baby.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
            level.addFreshEntity(baby);
        }
    }
}
package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.tucas.sculkeritegreatsword.entity.custom.PeekerEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

public class PeekerFetusBlock extends Block implements BonemealableBlock {

    private static final int HATCH_CHANCE_ONE_IN = 15;
    private static final float BONEMEAL_HATCH_CHANCE = 0.8F; // 10% por cada polvo de hueso

    public PeekerFetusBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // --- Eclosión natural ---

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(HATCH_CHANCE_ONE_IN) == 0) {
            hatch(level, pos);
        }
    }

    // --- Polvo de hueso ---

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < BONEMEAL_HATCH_CHANCE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        hatch(level, pos);
    }

    // --- Eclosión ---

    private void hatch(ServerLevel level, BlockPos pos) {
        PeekerEntity baby = ModEntities.PEEKER.get().create(level);
        if (baby == null) {
            return; // no se pudo crear la entidad: el bloque se conserva
        }

        level.removeBlock(pos, false);

        baby.setAge(-24000); // nace bebé (requiere que PeekerEntity sea AgeableMob/Animal)
        baby.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        baby.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
        level.addFreshEntity(baby);
    }
}
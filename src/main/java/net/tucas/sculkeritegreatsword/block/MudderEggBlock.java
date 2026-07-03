package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import org.jetbrains.annotations.NotNull;

public class MudderEggBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public MudderEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter,
                                        @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level,
                     @NotNull BlockPos pos, @NotNull RandomSource random) {

        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }

        level.destroyBlock(pos, false);

        int count = rollBabies(random);

        for (int i = 0; i < count; i++) {
            var entity = ModEntities.MUDDER.get().create(level);
            if (entity != null) {
                entity.setBaby(true);
                double offsetX = (random.nextDouble() - 0.5) * 0.6;
                double offsetZ = (random.nextDouble() - 0.5) * 0.6;
                entity.moveTo(pos.getX() + 0.5 + offsetX, pos.getY() + 0.5, pos.getZ() + 0.5 + offsetZ, 0, 0);
                ForgeEventFactory.onFinalizeSpawn(entity, level,
                        level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
                level.addFreshEntity(entity);
            }
        }
    }

    // 1 → 60%  |  2 → 25%  |  3 → 10%  |  4 → 4%  |  5 → 1%
    private int rollBabies(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 60) return 1;
        if (roll < 85) return 2;
        if (roll < 95) return 3;
        if (roll < 99) return 4;
        return 5;
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                        @NotNull BlockState oldState, boolean b) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 18000);
        }
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return (below.is(Blocks.MUD) || below.is(Blocks.DIRT) || below.is(Blocks.SAND))
                && below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction dir,
                                           @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }
}
package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BullsquamaEggBlock extends BaseEntityBlock {

    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);

    public BullsquamaEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HATCH, 0));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull BlockGetter getter,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void tick(@NotNull BlockState state,
                     @NotNull ServerLevel level,
                     @NotNull BlockPos pos,
                     @NotNull RandomSource random) {

        int hatch = state.getValue(HATCH);

        if (hatch == 0) {

            level.playSound(null, pos,
                    SoundEvents.SNIFFER_EGG_CRACK,
                    SoundSource.BLOCKS,
                    0.7F,
                    0.9F + random.nextFloat() * 0.2F);

            level.setBlock(pos, state.setValue(HATCH, 1), 2);

            level.scheduleTick(pos, this, 12000); // 10 min

        } else if (hatch == 1) {

            level.playSound(null, pos,
                    SoundEvents.SNIFFER_EGG_CRACK,
                    SoundSource.BLOCKS,
                    0.7F,
                    0.9F + random.nextFloat() * 0.2F);

            level.setBlock(pos, state.setValue(HATCH, 2), 2);

            level.scheduleTick(pos, this, 12000); // 10 min

        } else {

            hatch(level, pos, random); // 5 min final
        }
    }

    @Override
    public void onPlace(@NotNull BlockState state,
                        @NotNull Level level,
                        @NotNull BlockPos pos,
                        @NotNull BlockState oldState,
                        boolean moving) {

        if (!level.isClientSide) {

            level.scheduleTick(pos, this, 12000);

        }
    }

    private void hatch(ServerLevel level,
                       BlockPos pos,
                       RandomSource random) {

        level.playSound(null, pos,
                SoundEvents.SNIFFER_EGG_HATCH,
                SoundSource.BLOCKS,
                0.7F,
                0.9F + random.nextFloat() * 0.2F);

        level.destroyBlock(pos, false);

        Entity entity = ModEntities.BULLSQUAMA.get().create(level);

        if (entity instanceof Mob mob) {

            if (entity instanceof Animal animal) {
                animal.setBaby(true);
            }

            entity.moveTo(
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    random.nextFloat() * 360F,
                    0F
            );

            level.addFreshEntity(entity);

            ForgeEventFactory.onFinalizeSpawn(
                    mob,
                    level,
                    level.getCurrentDifficultyAt(pos),
                    MobSpawnType.NATURAL,
                    null,
                    null
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state,
                                  @NotNull BlockGetter getter,
                                  @NotNull BlockPos pos,
                                  @NotNull PathComputationType type) {
        return false;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos,
                                      @NotNull BlockState state) {
        return null;
    }
}
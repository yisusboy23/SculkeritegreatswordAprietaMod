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
public class DrillerEggBlock extends BaseEntityBlock {

    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;

    // Ajusta estos px según el tamaño visual del huevo que diseñes
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public DrillerEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HATCH, 0));
    }

    // ─── Hitbox ──────────────────────────────────────────────────────────────
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter,
                                        @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    // ─── Lógica de eclosión ──────────────────────────────────────────────────
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int hatch = state.getValue(HATCH);

        if (hatch < 2) {

            level.playSound(null, pos,
                    SoundEvents.SNIFFER_EGG_CRACK,
                    SoundSource.BLOCKS,
                    0.7F,
                    0.9F + random.nextFloat() * 0.2F);

            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);

            int delay;

            if (hatch == 0) {
                // 2do crack a los 10 min desde el inicio (5 min después del primero)
                delay = 6000;
            } else {
                // 3ro (nace) 5 min después
                delay = 6000;
            }

            level.scheduleTick(pos, this, delay);

        } else {
            this.hatch(level, pos, random);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide) {
            // 1er crack en 5 min
            level.scheduleTick(pos, this, 6000);
        }
    }

    private void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        level.playSound(null, pos,
                SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS,
                0.7F, 0.9F + random.nextFloat() * 0.2F);
        level.destroyBlock(pos, false);

        Entity entity = ModEntities.DRILLER.get().create(level);
        if (entity instanceof Mob mob) {
            if (entity instanceof Animal animal) {
                animal.setBaby(true);  // ← nace bebé
            }
            entity.moveTo(
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    level.random.nextFloat() * 360.0F, 0.0F
            );
            level.addFreshEntity(entity);
            ForgeEventFactory.onFinalizeSpawn(mob, level,
                    level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
        }
    }

    // ─── Boilerplate ─────────────────────────────────────────────────────────
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter getter,
                                  @NotNull BlockPos pos, @NotNull PathComputationType type) {
        return false;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        // null = no necesita BlockEntity propio (sin dueño como el original)
        return null;
    }
}
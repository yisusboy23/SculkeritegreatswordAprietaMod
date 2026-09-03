package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tucas.sculkeritegreatsword.entity.custom.MandrakeEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

import java.util.Collections;
import java.util.List;

public class MandrakeRootBlock extends BushBlock {

    private static final VoxelShape SHAPE = Shapes.box(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.4375D, 0.6875D);

    public MandrakeRootBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.MUD
                || block == Blocks.MOSS_BLOCK || block == Blocks.PODZOL;
    }

    // Nunca suelta el ítem, sin importar cómo se rompa (mano, herramienta, encantamiento de fortuna, etc.)
    @Override
    public List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    // Rotura por jugador: no spawnea aquí, lo hace onRemove (evita duplicados)
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
    }

    // Único punto de spawn: cubre jugador, explosión, pistón, fuego, etc.
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            spawnMandrake(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void spawnMandrake(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            MandrakeEntity mandrake = ModEntities.MANDRAKE.get().create(serverLevel);
            if (mandrake != null) {
                mandrake.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                serverLevel.addFreshEntity(mandrake);
            }
        }
    }
}
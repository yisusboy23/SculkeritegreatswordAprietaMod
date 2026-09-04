package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tucas.sculkeritegreatsword.entity.custom.MandrakeEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.item.Moditems;

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

    // Interacción con Brocha: Cosecha la raíz limpiamente
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof BrushItem) {
            if (!level.isClientSide) {
                popResource(level, pos, new ItemStack(Moditems.MANDRAKE_ROOT.get()));
                level.destroyBlock(pos, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    // Romper con la mano/herramienta: Spawnea la entidad (excepto si fue con brocha)
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (!(player.getMainHandItem().getItem() instanceof BrushItem)) {
                MandrakeEntity mandrake = ModEntities.MANDRAKE.get().create(serverLevel);
                if (mandrake != null) {
                    mandrake.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    serverLevel.addFreshEntity(mandrake);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
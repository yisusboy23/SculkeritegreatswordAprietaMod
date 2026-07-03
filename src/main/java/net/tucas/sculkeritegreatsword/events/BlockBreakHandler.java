package net.tucas.sculkeritegreatsword.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.init.ModEnchantments;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.tags.BlockTags;
import net.tucas.sculkeritegreatsword.item.PaxelItem;

@Mod.EventBusSubscriber(modid = "sculkeritegreatsword")
public class BlockBreakHandler {

    // Bandera para evitar recursión infinita
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    // Set para rastrear bloques que ya están siendo procesados
    private static final ThreadLocal<Set<BlockPos>> PROCESSED_BLOCKS = ThreadLocal.withInitial(HashSet::new);

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // Si ya estamos procesando, ignorar para evitar recursión
        if (IS_PROCESSING.get()) {
            return;
        }

        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        BlockPos origin = event.getPos();

        // Validaciones básicas
        if (level.isClientSide || player == null || player.isCrouching()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty()) return;

        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WIDE_DIGGING.get(), tool);
        if (enchantLevel <= 0) return;

        Direction face = getHitFace(level, player);
        if (face == null) return;

        // Obtener el bloque original que se rompió
        BlockState originalState = event.getState();
        Block originalBlock = originalState.getBlock();

        // Determinar si el bloque original es "duro" (obsidiana, bedrock, etc.)
        boolean isHardBlock = isHardBlock(originalState, serverLevel, origin);

        try {
            // Marcar que estamos procesando
            IS_PROCESSING.set(true);
            PROCESSED_BLOCKS.get().clear();
            PROCESSED_BLOCKS.get().add(origin);

            List<BlockPos> area = getAffectedBlocks(origin, enchantLevel, face);

            for (BlockPos pos : area) {
                if (pos.equals(origin)) continue;
                if (PROCESSED_BLOCKS.get().contains(pos)) continue;

                try {
                    BlockState state = serverLevel.getBlockState(pos);
                    if (state.isAir()) continue;

                    // Si el bloque original es "duro", solo romper bloques del mismo tipo
                    if (isHardBlock && state.getBlock() != originalBlock) continue;

                    // Si el bloque actual es "duro" pero NO rompiste un bloque duro, no lo rompas
                    if (!isHardBlock && isHardBlock(state, serverLevel, pos)) continue;

                    // Verificar si se puede romper el bloque con esta herramienta
                    if (!canBreakBlock(state, tool, player, pos, serverLevel)) continue;

                    // Marcar como procesado
                    PROCESSED_BLOCKS.get().add(pos);

                    // Obtener drops con encantamientos (Silk Touch, Fortune, etc.)
                    BlockEntity blockEntity = state.hasBlockEntity() ? serverLevel.getBlockEntity(pos) : null;
                    Block.dropResources(state, serverLevel, pos, blockEntity, player, tool);

                    // Romper el bloque SIN disparar evento
                    serverLevel.destroyBlock(pos, false);

                    // Dañar herramienta
                    if (!player.isCreative() && !tool.isEmpty()) {
                        tool.hurtAndBreak(1, serverPlayer, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
                    }

                } catch (Exception e) {
                    // Si algo falla con un bloque, continuar con el siguiente
                    System.err.println("Error breaking block at " + pos + ": " + e.getMessage());
                }
            }
        } finally {
            // IMPORTANTE: Siempre limpiar la bandera
            IS_PROCESSING.set(false);
            PROCESSED_BLOCKS.get().clear();
        }
    }

    private static boolean canBreakBlock(BlockState state, ItemStack tool, Player player, BlockPos pos, Level level) {
        // Verificar que el bloque no sea indestructible
        if (state.getDestroySpeed(level, pos) < 0) {
            return false;
        }

        // NUEVO: Para PaxelItem, verificar manualmente los 3 tipos
        if (tool.getItem() instanceof net.tucas.sculkeritegreatsword.item.PaxelItem) {
            boolean canMine = state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE) ||
                    state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE) ||
                    state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL);

            if (!canMine) return false;

            // Verificar tier correcto
            return player.hasCorrectToolForDrops(state);
        }

        // Para otras herramientas, usar el método normal
        if (!tool.isCorrectToolForDrops(state)) {
            return false;
        }

        return player.hasCorrectToolForDrops(state);
    }

    private static boolean isHardBlock(BlockState state, Level level, BlockPos pos) {
        // Un bloque es "duro" si tarda mucho en romperse (hardness alto)
        // Obsidiana tiene hardness de 50, piedra normal tiene 1.5
        float hardness = state.getDestroySpeed(level, pos);

        // Considerar "duro" si hardness >= 10 (obsidiana=50, crying obsidiana=50, etc)
        return hardness >= 10.0f;
    }

    private static Direction getHitFace(Level level, Player player) {
        try {
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 reachVec = eyePos.add(lookVec.scale(player.getBlockReach()));

            BlockHitResult result = level.clip(new ClipContext(
                    eyePos, reachVec,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            return result.getType() == BlockHitResult.Type.BLOCK ? result.getDirection() : null;
        } catch (Exception e) {
            return Direction.UP;
        }
    }

    private static List<BlockPos> getAffectedBlocks(BlockPos center, int enchantLevel, Direction face) {
        List<BlockPos> result = new ArrayList<>();

        if (enchantLevel == 1) {
            // Para nivel 1 (2x2)
            Vec3i[] axes = getPerpendicularAxes(face);
            Vec3i axis1 = axes[0];
            Vec3i axis2 = axes[1];

            for (int i = 0; i <= 1; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;

                    BlockPos offset = center.offset(
                            axis1.getX() * i + axis2.getX() * j,
                            axis1.getY() * i + axis2.getY() * j,
                            axis1.getZ() * i + axis2.getZ() * j
                    );

                    result.add(offset);
                }
            }
        } else {
            // Para nivel 2+ (3x3)
            int start = -1;
            int end = 1;

            Vec3i[] axes = getPerpendicularAxes(face);
            Vec3i axis1 = axes[0];
            Vec3i axis2 = axes[1];

            for (int i = start; i <= end; i++) {
                for (int j = start; j <= end; j++) {
                    if (i == 0 && j == 0) continue;

                    BlockPos offset = center.offset(
                            axis1.getX() * i + axis2.getX() * j,
                            axis1.getY() * i + axis2.getY() * j,
                            axis1.getZ() * i + axis2.getZ() * j
                    );

                    result.add(offset);
                }
            }
        }

        return result;
    }

    private static Vec3i[] getPerpendicularAxes(Direction face) {
        switch (face) {
            case UP:
            case DOWN:
                return new Vec3i[]{Direction.EAST.getNormal(), Direction.NORTH.getNormal()};

            case NORTH:
            case SOUTH:
                return new Vec3i[]{Direction.EAST.getNormal(), Direction.UP.getNormal()};

            case EAST:
            case WEST:
                return new Vec3i[]{Direction.NORTH.getNormal(), Direction.UP.getNormal()};

            default:
                return new Vec3i[]{Direction.EAST.getNormal(), Direction.UP.getNormal()};
        }
    }
}
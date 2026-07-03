package net.tucas.sculkeritegreatsword.procedures;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.tucas.sculkeritegreatsword.entity.custom.HydrantorGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.tucas.sculkeritegreatsword.init.ModEntities;

@Mod.EventBusSubscriber
public class HydrantorGolemSpawnProcedure {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        if (!world.getBlockState(pos).is(Blocks.CARVED_PUMPKIN)) {
            return;
        }

        // Verificar estructura y obtener variante
        GolemVariant variant = isValidStructure(world, pos);
        if (variant == GolemVariant.INVALID) {
            return;
        }

        spawnGolem(world, pos, player, variant);
        destroyStructure(world, pos);
    }

    private enum GolemVariant {
        INVALID,
        NORMAL,   // Centro capa 2 es cobre normal
        BODY1     // Centro capa 2 es GEAR_COPPER_BLOCK
    }

    /**
     * CAPA 3: [o][o][o] / [o][🎃][o] / [o][o][o]   (o = aire)
     * CAPA 2: [C][C][C] / [C][d][C] / [C][C][C]     (d = GEAR_COPPER_BLOCK, C = cualquier cobre)
     * CAPA 1: [E,C][E][E,C] / [E,C][E][E,C] / [E,C][E][E,C] (E = stone, C = cobre)
     */
    private static GolemVariant isValidStructure(LevelAccessor world, BlockPos pumpkin) {
        BlockPos layer2Center = pumpkin.below();
        BlockPos layer1Center = pumpkin.below(2);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Saltar la calabaza

                BlockPos checkPos = pumpkin.offset(x, 0, z);
                if (!world.getBlockState(checkPos).is(Blocks.AIR)) {
                    System.out.println("DEBUG: Hydrantor - Capa 3 no es aire en x=" + x + " z=" + z);
                    return GolemVariant.INVALID;
                }
            }
        }

        boolean isBody1 = world.getBlockState(layer2Center).is(ModBlocks.GEAR_COPPER_BLOCK.get());

        // Verificar que el centro sea válido (cobre normal O gear block)
        if (!isBody1 && !isCopperBlock(world.getBlockState(layer2Center).getBlock())) {
            System.out.println("DEBUG: Hydrantor - Centro capa 2 no es cobre ni gear block");
            return GolemVariant.INVALID;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Saltar el centro

                BlockPos checkPos = layer2Center.offset(x, 0, z);
                if (!isCopperBlock(world.getBlockState(checkPos).getBlock())) {
                    System.out.println("DEBUG: Hydrantor - Capa 2 bloque no es cobre en x=" + x + " z=" + z);
                    return GolemVariant.INVALID;
                }
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = layer1Center.offset(x, 0, z);
                Block block = world.getBlockState(checkPos).getBlock();

                if (x == 0) {
                    if (!isStoneBlock(block)) {
                        System.out.println("DEBUG: Hydrantor - Capa 1 requiere STONE en x=" + x + " z=" + z + " pero es: " + block);
                        return GolemVariant.INVALID;
                    }
                }
                // Esquinas (x = +/-1, z = +/-1) pueden ser piedra O cobre
                else if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                    if (!isStoneBlock(block) && !isCopperBlock(block)) {
                        System.out.println("DEBUG: Hydrantor - Esquina capa 1 inválida en x=" + x + " z=" + z);
                        return GolemVariant.INVALID;
                    }
                }

                else {
                    if (!isStoneBlock(block)) {
                        System.out.println("DEBUG: Hydrantor - Borde lateral capa 1 requiere STONE en x=" + x + " z=" + z);
                        return GolemVariant.INVALID;
                    }
                }
            }
        }

        // Determinar variante
        GolemVariant variant = isBody1 ? GolemVariant.BODY1 : GolemVariant.NORMAL;
        System.out.println("✓ Hydrantor Golem - Estructura válida! Variante: " + variant);
        return variant;
    }

    private static boolean isCopperBlock(Block block) {
        return block == Blocks.COPPER_BLOCK ||
                block == Blocks.EXPOSED_COPPER ||
                block == Blocks.WEATHERED_COPPER ||
                block == Blocks.OXIDIZED_COPPER ||
                block == Blocks.CUT_COPPER ||
                block == Blocks.EXPOSED_CUT_COPPER ||
                block == Blocks.WEATHERED_CUT_COPPER ||
                block == Blocks.OXIDIZED_CUT_COPPER ||
                block == Blocks.WAXED_COPPER_BLOCK ||
                block == Blocks.WAXED_EXPOSED_COPPER ||
                block == Blocks.WAXED_WEATHERED_COPPER ||
                block == Blocks.WAXED_OXIDIZED_COPPER ||
                block == Blocks.WAXED_CUT_COPPER ||
                block == Blocks.WAXED_EXPOSED_CUT_COPPER ||
                block == Blocks.WAXED_WEATHERED_CUT_COPPER ||
                block == Blocks.WAXED_OXIDIZED_CUT_COPPER;
    }
    private static boolean isStoneBlock(Block block) {
        return block == Blocks.STONE ||
                block == Blocks.ANDESITE ||
                block == Blocks.GRANITE ||
                block == Blocks.DIORITE ||
                block == Blocks.COBBLESTONE ||
                block == Blocks.MOSSY_COBBLESTONE ||
                block == Blocks.STONE_BRICKS ||
                block == Blocks.MOSSY_STONE_BRICKS ||
                block == Blocks.CRACKED_STONE_BRICKS ||
                block == Blocks.CHISELED_STONE_BRICKS ||
                block == Blocks.DEEPSLATE ||
                block == Blocks.COBBLED_DEEPSLATE ||
                block == Blocks.POLISHED_DEEPSLATE ||
                block == Blocks.DEEPSLATE_BRICKS ||
                block == Blocks.CRACKED_DEEPSLATE_BRICKS ||
                block == Blocks.DEEPSLATE_TILES ||
                block == Blocks.CRACKED_DEEPSLATE_TILES ||
                block == Blocks.CHISELED_DEEPSLATE ||
                block == Blocks.TUFF ||
                block == Blocks.CALCITE;
    }

    private static void spawnGolem(LevelAccessor world, BlockPos pumpkin, Player player, GolemVariant variant) {
        if (!(world instanceof ServerLevel serverLevel)) return;
        BlockPos spawnPos = pumpkin.below();

        HydrantorGolemEntity golem = ModEntities.HYDRANTOR_GOLEM.get().create(serverLevel);
        if (golem != null) {
            golem.moveTo(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    world.getRandom().nextFloat() * 360.0f,
                    0.0f
            );

            golem.getPersistentData().putBoolean("isBody1Variant", variant == GolemVariant.BODY1);

            golem.tame(player);
            golem.setOrderedToSit(false);
            serverLevel.addFreshEntity(golem);
            System.out.println("✓ Hydrantor Golem spawned and tamed to " + player.getName().getString() +
                    "! Variante: " + variant);
        }
    }

    private static void destroyStructure(LevelAccessor world, BlockPos pumpkin) {
        BlockPos layer2Center = pumpkin.below();
        BlockPos layer1Center = pumpkin.below(2);

        world.destroyBlock(pumpkin, false);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                world.destroyBlock(pumpkin.offset(x, 0, z), false);
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.destroyBlock(layer2Center.offset(x, 0, z), false);
            }
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.destroyBlock(layer1Center.offset(x, 0, z), false);
            }
        }

        System.out.println("✓ Hydrantor Golem structure destroyed");
    }
}
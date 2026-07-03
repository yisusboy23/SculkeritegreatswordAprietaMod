package net.tucas.sculkeritegreatsword.procedures;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.tucas.sculkeritegreatsword.entity.custom.DiamondGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

@Mod.EventBusSubscriber
public class DiamondGolemSpawnProcedure {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        // Solo activar si colocas Carved Pumpkin
        if (!world.getBlockState(pos).is(Blocks.CARVED_PUMPKIN)) {
            return;
        }
        // Verificar estructura
        if (!isValidStructure(world, pos)) {
            return;
        }
        // Spawnear golem
        spawnGolem(world, pos, player);
        // Destruir estructura
        destroyStructure(world, pos);
    }

    private static boolean isValidStructure(LevelAccessor world, BlockPos pumpkin) {
        BlockPos center = pumpkin.below();      // Centro (C) - 1 bloque abajo
        BlockPos left = center.west();          // Brazo izquierdo (B)
        BlockPos right = center.east();         // Brazo derecho (B)
        BlockPos bottom = center.below();       // Base (B) - 2 bloques abajo del pumpkin

        // Verificar bloques
        boolean centerIsOre = world.getBlockState(center).is(Blocks.DIAMOND_ORE) ||
                world.getBlockState(center).is(Blocks.DEEPSLATE_DIAMOND_ORE);
        boolean leftIsDiamondBlock = world.getBlockState(left).is(Blocks.DIAMOND_BLOCK);
        boolean rightIsDiamondBlock = world.getBlockState(right).is(Blocks.DIAMOND_BLOCK);
        boolean bottomIsDiamondBlock = world.getBlockState(bottom).is(Blocks.DIAMOND_BLOCK);

        // Verificar orientación Norte-Sur también
        BlockPos leftNS = center.north();
        BlockPos rightNS = center.south();
        boolean leftIsDiamondBlockNS = world.getBlockState(leftNS).is(Blocks.DIAMOND_BLOCK);
        boolean rightIsDiamondBlockNS = world.getBlockState(rightNS).is(Blocks.DIAMOND_BLOCK);

        boolean validWestEast = centerIsOre && leftIsDiamondBlock && rightIsDiamondBlock && bottomIsDiamondBlock;
        boolean validNorthSouth = centerIsOre && leftIsDiamondBlockNS && rightIsDiamondBlockNS && bottomIsDiamondBlock;

        return validWestEast || validNorthSouth;
    }

    private static void spawnGolem(LevelAccessor world, BlockPos pumpkin, Player player) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        // Spawnear en la base (2 bloques abajo del pumpkin)
        BlockPos spawnPos = pumpkin.below(2);

        DiamondGolemEntity golem = ModEntities.DIAMOND_GOLEM.get().create(serverLevel);
        if (golem != null) {
            golem.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    world.getRandom().nextFloat() * 360.0f, 0.0f);
            golem.tame(player);
            golem.getPersistentData().putInt("golem_mode", 1); // FOLLOW MODE por defecto
            serverLevel.addFreshEntity(golem);

            System.out.println("✓ Diamond Golem spawned and tamed!");
        }
    }

    private static void destroyStructure(LevelAccessor world, BlockPos pumpkin) {
        BlockPos center = pumpkin.below();
        BlockPos bottom = center.below();

        // Eliminar cabeza (pumpkin)
        world.destroyBlock(pumpkin, false);

        // Eliminar centro (ore)
        world.destroyBlock(center, false);

        world.destroyBlock(bottom, false);
        world.destroyBlock(center.west(), false);   // Oeste
        world.destroyBlock(center.east(), false);   // Este
        world.destroyBlock(center.north(), false);  // Norte
        world.destroyBlock(center.south(), false);  // Sur
    }
}
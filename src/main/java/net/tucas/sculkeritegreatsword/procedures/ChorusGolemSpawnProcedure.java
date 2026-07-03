package net.tucas.sculkeritegreatsword.procedures;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.tucas.sculkeritegreatsword.entity.custom.ChorusGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

@Mod.EventBusSubscriber
public class ChorusGolemSpawnProcedure {

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

    /**
     * Verifica si la estructura es válida:
     * - Capa 1 (bottom): 3x3 de End Stone o End Stone Bricks
     * - Capa 2 (middle): 3x3 de End Stone o End Stone Bricks
     * - Capa 3 (top): Calabaza en el centro, mínimo 4 Chorus Flowers/Plants alrededor
     */
    private static boolean isValidStructure(LevelAccessor world, BlockPos pumpkin) {
        // El pumpkin está en la capa superior (capa 3)
        // Capa 2 está 1 bloque abajo
        // Capa 1 está 2 bloques abajo

        BlockPos layer2Center = pumpkin.below();     // Centro de capa 2
        BlockPos layer1Center = pumpkin.below(2);    // Centro de capa 1

        // Verificar capa 1 (base) - debe ser 3x3 de End Stone o End Stone Bricks
        if (!isValidEndStoneLayer(world, layer1Center)) {
            return false;
        }

        // Verificar capa 2 (medio) - debe ser 3x3 de End Stone o End Stone Bricks
        if (!isValidEndStoneLayer(world, layer2Center)) {
            return false;
        }

        // Verificar capa 3 (top) - calabaza en centro + mínimo 4 chorus flowers/plants
        int chorusCount = countChorusBlocks(world, pumpkin);

        if (chorusCount < 4) {
            System.out.println("DEBUG: Chorus Golem - Insuficientes bloques de Chorus: " + chorusCount + "/4");
            return false;
        }

        System.out.println("✓ Chorus Golem - Estructura válida detectada! Bloques de Chorus: " + chorusCount);
        return true;
    }

    /**
     * Verifica si una capa 3x3 está completamente hecha de End Stone o End Stone Bricks
     */
    private static boolean isValidEndStoneLayer(LevelAccessor world, BlockPos center) {
        // Verificar los 9 bloques de la capa 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                boolean isEndStone = world.getBlockState(checkPos).is(Blocks.END_STONE);
                boolean isEndStoneBricks = world.getBlockState(checkPos).is(Blocks.END_STONE_BRICKS);

                if (!isEndStone && !isEndStoneBricks) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Cuenta cuántos bloques de Chorus Flower o Chorus Plant hay alrededor del pumpkin
     */
    private static int countChorusBlocks(LevelAccessor world, BlockPos pumpkin) {
        int count = 0;

        // Verificar los 8 bloques alrededor del pumpkin (mismo nivel Y)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Saltar el centro (donde está el pumpkin)
                if (x == 0 && z == 0) {
                    continue;
                }

                BlockPos checkPos = pumpkin.offset(x, 0, z);
                boolean isChorusFlower = world.getBlockState(checkPos).is(Blocks.CHORUS_FLOWER);
                boolean isChorusPlant = world.getBlockState(checkPos).is(Blocks.CHORUS_PLANT);

                if (isChorusFlower || isChorusPlant) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Spawnea el Chorus Golem en la ubicación de la estructura
     */
    private static void spawnGolem(LevelAccessor world, BlockPos pumpkin, Player player) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        // Spawnear en la capa 2 (centro de la estructura)
        BlockPos spawnPos = pumpkin.below();

        ChorusGolemEntity golem = ModEntities.CHORUS_GOLEM.get().create(serverLevel);
        if (golem != null) {
            golem.moveTo(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    world.getRandom().nextFloat() * 360.0f,
                    0.0f
            );

            // Domesticar al golem automáticamente
            golem.tame(player);
            golem.setOrderedToSit(false); // Que siga al jugador por defecto

            serverLevel.addFreshEntity(golem);

            System.out.println("✓ Chorus Golem spawned and tamed to " + player.getName().getString() + "!");
        }
    }

    /**
     * Destruye la estructura completa después de spawnear el golem
     */
    private static void destroyStructure(LevelAccessor world, BlockPos pumpkin) {
        // Destruir calabaza
        world.destroyBlock(pumpkin, false);

        BlockPos layer2Center = pumpkin.below();
        BlockPos layer1Center = pumpkin.below(2);

        // Destruir capa 3 (top) - pumpkin y chorus blocks alrededor
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = pumpkin.offset(x, 0, z);
                world.destroyBlock(pos, false);
            }
        }

        // Destruir capa 2 (middle) - 3x3 de End Stone
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = layer2Center.offset(x, 0, z);
                world.destroyBlock(pos, false);
            }
        }

        // Destruir capa 1 (bottom) - 3x3 de End Stone
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = layer1Center.offset(x, 0, z);
                world.destroyBlock(pos, false);
            }
        }

        System.out.println("✓ Chorus Golem structure destroyed");
    }
}
package net.tucas.sculkeritegreatsword.procedures;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

@Mod.EventBusSubscriber
public class MushroomGolemSpawnProcedure {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState placedBlock = world.getBlockState(pos);

        // Detectar tipo de hongo
        MushroomGolemEntity.Variant variant = null;

        if (isRedMushroom(placedBlock)) {
            variant = MushroomGolemEntity.Variant.RED;
            System.out.println("🍄 Red mushroom detected!");
        } else if (isBrownMushroom(placedBlock)) {
            variant = MushroomGolemEntity.Variant.BROWN;
            System.out.println("🟫 Brown mushroom detected!");
        } else {
            return; // No es ningún tipo de hongo
        }

        // Verificar estructura desde este hongo (centro)
        if (isValidStructure(world, pos, variant)) {
            System.out.println("✅ Valid structure at CENTER!");
            spawnGolem(world, pos, player, variant);
            destroyStructure(world, pos);
            return;
        }

        // Verificar East-West
        if (isValidStructure(world, pos.west(), variant)) {
            System.out.println("✅ Valid structure at WEST!");
            spawnGolem(world, pos.west(), player, variant);
            destroyStructure(world, pos.west());
            return;
        }
        if (isValidStructure(world, pos.east(), variant)) {
            System.out.println("✅ Valid structure at EAST!");
            spawnGolem(world, pos.east(), player, variant);
            destroyStructure(world, pos.east());
            return;
        }

        // Verificar North-South
        if (isValidStructure(world, pos.north(), variant)) {
            System.out.println("✅ Valid structure at NORTH!");
            spawnGolem(world, pos.north(), player, variant);
            destroyStructure(world, pos.north());
            return;
        }
        if (isValidStructure(world, pos.south(), variant)) {
            System.out.println("✅ Valid structure at SOUTH!");
            spawnGolem(world, pos.south(), player, variant);
            destroyStructure(world, pos.south());
        }
    }

    private static boolean isRedMushroom(BlockState state) {
        return state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.RED_MUSHROOM_BLOCK);
    }

    private static boolean isBrownMushroom(BlockState state) {
        return state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM_BLOCK);
    }

    private static boolean isMushroomOfType(BlockState state, MushroomGolemEntity.Variant variant) {
        return variant == MushroomGolemEntity.Variant.RED ? isRedMushroom(state) : isBrownMushroom(state);
    }

    private static boolean isValidStructure(LevelAccessor world, BlockPos center, MushroomGolemEntity.Variant variant) {
        // Calabaza debajo del hongo
        BlockPos pumpkin = center.below();
        if (!world.getBlockState(pumpkin).is(Blocks.CARVED_PUMPKIN)) {
            return false;
        }

        // Mushroom stem debajo de la calabaza
        BlockPos stem = pumpkin.below();
        if (!world.getBlockState(stem).is(Blocks.MUSHROOM_STEM)) {
            return false;
        }

        // Verificar 3 hongos del MISMO TIPO (East-West)
        boolean eastWest =
                isMushroomOfType(world.getBlockState(center.west()), variant) &&
                        isMushroomOfType(world.getBlockState(center), variant) &&
                        isMushroomOfType(world.getBlockState(center.east()), variant);

        // Verificar 3 hongos del MISMO TIPO (North-South)
        boolean northSouth =
                isMushroomOfType(world.getBlockState(center.north()), variant) &&
                        isMushroomOfType(world.getBlockState(center), variant) &&
                        isMushroomOfType(world.getBlockState(center.south()), variant);

        return eastWest || northSouth;
    }

    private static void spawnGolem(LevelAccessor world, BlockPos center, Player player, MushroomGolemEntity.Variant variant) {
        if (!(world instanceof ServerLevel serverLevel)) return;

        BlockPos spawnPos = center.below(2);

        MushroomGolemEntity golem = ModEntities.MUSHROOM_GOLEM.get().create(serverLevel);
        if (golem != null) {
            // IMPORTANTE: Asignar variante
            golem.setVariant(variant);

            golem.moveTo(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    world.getRandom().nextFloat() * 360F,
                    0F
            );
            golem.tame(player);
            golem.getPersistentData().putInt("golem_mode", 3);
            golem.setPersistenceRequired();
            serverLevel.addFreshEntity(golem);

            System.out.println("✅✅✅ Mushroom Golem spawned with variant: " + variant + " ✅✅✅");
        }
    }

    private static void destroyStructure(LevelAccessor world, BlockPos center) {
        BlockPos pumpkin = center.below();
        BlockPos stem = pumpkin.below();

        // Romper hongos
        world.destroyBlock(center.east(), false);
        world.destroyBlock(center, false);
        world.destroyBlock(center.west(), false);
        world.destroyBlock(center.north(), false);
        world.destroyBlock(center.south(), false);

        // Romper calabaza y stem
        world.destroyBlock(pumpkin, false);
        world.destroyBlock(stem, false);
    }
}

package net.tucas.sculkeritegreatsword.procedures;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.init.ModBlocks;

@Mod.EventBusSubscriber
public class OxicopterGolemSpawnProcedure {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        // Se activa al colocar la calabaza tallada
        if (!world.getBlockState(pos).is(Blocks.CARVED_PUMPKIN)) {
            return;
        }

        if (!isValidStructure(world, pos)) {
            return;
        }

        spawnGolem(world, pos, player);
        destroyStructure(world, pos);
    }

    private static boolean isValidStructure(LevelAccessor world, BlockPos head) {
        boolean body1 = world.getBlockState(head.below()).is(ModBlocks.GEAR_COPPER_BLOCK.get());
        boolean body2 = world.getBlockState(head.below(2)).is(Blocks.OXIDIZED_COPPER);

        return body1 && body2;
    }

    private static void spawnGolem(LevelAccessor world, BlockPos head, Player player) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos spawnPos = head.below(2);

        OxicopperGolemEntity golem = ModEntities.OXICOPPER_GOLEM.get().create(serverLevel);
        if (golem != null) {
            golem.moveTo(spawnPos.getX() + 0.5, (double) spawnPos.getY(), spawnPos.getZ() + 0.5,
                    world.getRandom().nextFloat() * 360.0f, 0.0f);
            golem.tame(player);
            golem.getPersistentData().putInt("golem_mode", 3);
            serverLevel.addFreshEntity(golem);

            System.out.println("Oxicopper Golem spawned!");
        }
    }

    private static void destroyStructure(LevelAccessor world, BlockPos head) {
        world.destroyBlock(head, false);           // Calabaza tallada
        world.destroyBlock(head.below(), false);   // Gear Copper Block
        world.destroyBlock(head.below(2), false);  // Oxidized Copper
    }
}

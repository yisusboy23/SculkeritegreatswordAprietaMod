package net.tucas.sculkeritegreatsword.procedures;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

@Mod.EventBusSubscriber
public class SculkGolemSpawnProcedure {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        // Solo activar si colocas Sculk Shrieker
        if (!world.getBlockState(pos).is(Blocks.SCULK_SHRIEKER)) {
            return;
        }

        // Verificar estructura (Shrieker arriba hacia abajo)
        if (!isValidStructure(world, pos)) {
            return;
        }

        // Spawnear golem
        spawnGolem(world, pos, player);

        // Destruir estructura
        destroyStructure(world, pos);
    }

    private static boolean isValidStructure(LevelAccessor world, BlockPos shrieker) {
        // Shrieker (arriba)
        // Pumpkin (1 abajo)
        // Deepslate (2 abajo - cuerpo)
        // Deepslate (3 abajo - base)
        // Catalyst (brazos en los lados del cuerpo)

        boolean head = world.getBlockState(shrieker.below()).is(Blocks.CARVED_PUMPKIN);
        boolean body1 = world.getBlockState(shrieker.below(2)).is(Blocks.DEEPSLATE);
        boolean body2 = world.getBlockState(shrieker.below(3)).is(Blocks.DEEPSLATE);

        // Verificar brazos (OESTE-ESTE o NORTE-SUR)
        BlockPos bodyPos = shrieker.below(2);
        boolean armsWestEast = world.getBlockState(bodyPos.west()).is(Blocks.SCULK_CATALYST) &&
                world.getBlockState(bodyPos.east()).is(Blocks.SCULK_CATALYST);
        boolean armsNorthSouth = world.getBlockState(bodyPos.north()).is(Blocks.SCULK_CATALYST) &&
                world.getBlockState(bodyPos.south()).is(Blocks.SCULK_CATALYST);

        return head && body1 && body2 && (armsWestEast || armsNorthSouth);
    }

    private static void spawnGolem(LevelAccessor world, BlockPos shrieker, Player player) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        // Spawnear en la base (3 bloques abajo del shrieker)
        BlockPos spawnPos = shrieker.below(3);

        SculkGolemEntity golem = ModEntities.SCULK_GOLEM.get().create(serverLevel);
        if (golem != null) {
            golem.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    world.getRandom().nextFloat() * 360.0f, 0.0f);
            golem.tame(player);
            golem.getPersistentData().putInt("golem_mode", 3);
            serverLevel.addFreshEntity(golem);

            System.out.println("✓ Sculk Golem spawned and tamed!");
        }
    }

    private static void destroyStructure(LevelAccessor world, BlockPos shrieker) {
        // Eliminar shrieker, cabeza y cuerpo
        world.destroyBlock(shrieker, false);
        world.destroyBlock(shrieker.below(), false);
        world.destroyBlock(shrieker.below(2), false);
        world.destroyBlock(shrieker.below(3), false);

        // Eliminar brazos (probar ambas orientaciones)
        BlockPos bodyPos = shrieker.below(2);
        world.destroyBlock(bodyPos.west(), false);
        world.destroyBlock(bodyPos.east(), false);
        world.destroyBlock(bodyPos.north(), false);
        world.destroyBlock(bodyPos.south(), false);
    }
}
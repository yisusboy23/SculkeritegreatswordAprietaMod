package net.tucas.sculkeritegreatsword.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.tucas.sculkeritegreatsword.init.ModPotions;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MandrakePotionImpactHandler {

    private static final int RADIUS = 3; // radio horizontal de efecto
    private static final int HEIGHT = 1; // radio vertical de efecto

    @SubscribeEvent
    public static void onPotionImpact(ProjectileImpactEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion thrownPotion)) return;

        Level level = thrownPotion.level();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        // Solo reacciona a nuestra poción específica (incluye splash y lingering, ambas son ThrownPotion)
        if (PotionUtils.getPotion(thrownPotion.getItem()) != ModPotions.MANDRAKE_SONG.get()) return;

        BlockPos center = thrownPotion.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -HEIGHT, -RADIUS),
                center.offset(RADIUS, HEIGHT, RADIUS))) {

            BlockState state = serverLevel.getBlockState(pos);

            // Reemplaza cualquier cultivo (trigo, zanahoria, papa, remolacha, etc.)
            if (state.getBlock() instanceof CropBlock) {
                serverLevel.setBlockAndUpdate(pos.immutable(), ModBlocks.MANDRAKE_ROOT_BLOCK.get().defaultBlockState());
            }
        }
    }
}
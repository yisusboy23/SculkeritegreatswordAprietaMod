package net.tucas.sculkeritegreatsword.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.item.Moditems;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID)
public class GolemDeathDropHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Solo se aplica si la entidad es un jugador
        if (entity instanceof Player && GolemExplosionTracker.markedForDeathByGolem.remove(entity.getUUID())) {
            entity.spawnAtLocation(Moditems.GOODBYE_TO_A_WORLD_MUSIC_DISC.get());
        }
    }
}

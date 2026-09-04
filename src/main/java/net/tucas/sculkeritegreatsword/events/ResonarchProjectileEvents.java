package net.tucas.sculkeritegreatsword.events;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID)
public class ResonarchProjectileEvents {

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ResonarchEntity resonarch)) return;

        Projectile projectile = event.getProjectile();
        resonarch.onProjectileImpact(projectile);

        // Cancela el impacto/daño vanilla: la física del proyectil
        // (congelación y luego reflejo) la controla ResonarchEntity.
        event.setCanceled(true);
    }
}

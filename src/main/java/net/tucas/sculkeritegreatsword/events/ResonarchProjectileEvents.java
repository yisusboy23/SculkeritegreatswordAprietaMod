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
        if (resonarch.level().isClientSide) return;

        Projectile projectile = event.getProjectile();

        if (!resonarch.willCatchProjectile()) {
            // El escudo no está atrapando ahora mismo (p. ej. STATE_REFLECT ya en
            // curso): dejamos que el golpe siga su curso normal, sin cancelar,
            // para que llegue a hurt() y haga daño de verdad.
            return;
        }

        resonarch.onProjectileImpact(projectile);

        // Cancela el impacto/daño vanilla: la física del proyectil
        // (congelación y luego reflejo) la controla ResonarchEntity.
        event.setCanceled(true);
    }
}
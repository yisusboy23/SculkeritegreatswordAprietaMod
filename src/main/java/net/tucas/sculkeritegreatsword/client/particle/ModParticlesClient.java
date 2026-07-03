package net.tucas.sculkeritegreatsword.client.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModParticles;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticlesClient {
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SCULKERITEGOLEMSWEEP.get(),
                (spriteSet) -> SculkeritegolemsweepParticle.provider(spriteSet));
        event.registerSpriteSet(ModParticles.SCULKEXPLOSION.get(),
                (spriteSet) -> SculkexplosionParticle.provider(spriteSet));
        event.registerSpriteSet(ModParticles.LAPIS_CONTROL.get(),
                (spriteSet) -> LapisControlParticle.provider(spriteSet));
    }
}
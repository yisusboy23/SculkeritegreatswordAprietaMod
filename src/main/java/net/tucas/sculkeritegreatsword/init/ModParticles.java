package net.tucas.sculkeritegreatsword.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SCULKERITEGOLEMSWEEP =
            PARTICLES.register("sculkeritegolemsweep", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> SCULKEXPLOSION =
            PARTICLES.register("sculkexplosion", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> LAPIS_CONTROL =
            PARTICLES.register("lapis_control", () -> new SimpleParticleType(false));
}
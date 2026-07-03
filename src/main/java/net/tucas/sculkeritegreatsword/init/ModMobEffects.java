package net.tucas.sculkeritegreatsword.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.potion.SculkeritegreatswordDashingMobEffect;
import net.tucas.sculkeritegreatsword.potion.SculkeritegreatswordkineticenergydashMobEffect;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<MobEffect> SCULKERITEGREATSWORD_DASHING =
            EFFECTS.register("sculkeritegreatsword_dashing", SculkeritegreatswordDashingMobEffect::new);

    public static final RegistryObject<MobEffect> SCULKERITEGREATSWORDKINETICENERGYDASH =
            EFFECTS.register("sculkeritegreatswordkineticenergydash", SculkeritegreatswordkineticenergydashMobEffect::new);
}
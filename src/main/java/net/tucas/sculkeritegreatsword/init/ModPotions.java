package net.tucas.sculkeritegreatsword.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.potion.MandrakeSongEffect;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<Potion> MANDRAKE_SONG =
            POTIONS.register("mandrake_song", () -> new Potion(
                    new MobEffectInstance(
                            ModMobEffects.MANDRAKE_SONG.get(),
                            MandrakeSongEffect.PLAYER_DURATION_TICKS,
                            0
                    )
            ));

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
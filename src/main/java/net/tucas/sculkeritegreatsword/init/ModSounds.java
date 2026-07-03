package net.tucas.sculkeritegreatsword.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<SoundEvent> SWORD_HIT =
            SOUNDS.register("sword_hit", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "sword_hit")));

    public static final RegistryObject<SoundEvent> SONIC_BOOM =
            SOUNDS.register("sonic_boom", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "sonic_boom")));

    public static final RegistryObject<SoundEvent> DASH_HIT =
            SOUNDS.register("dash_hit", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "dash_hit")));

    public static final RegistryObject<SoundEvent> EXPLOSION =
            SOUNDS.register("largeunderwaterexplosion190270mp3", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "largeunderwaterexplosion190270mp3")));

    public static final RegistryObject<SoundEvent> MUSIC_DISC_GOODBYE_TO_A_WORLD =
            SOUNDS.register("music_disc.goodbye_to_a_world", () ->
                    SoundEvent.createFixedRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "music_disc.goodbye_to_a_world"), 16.0F));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}

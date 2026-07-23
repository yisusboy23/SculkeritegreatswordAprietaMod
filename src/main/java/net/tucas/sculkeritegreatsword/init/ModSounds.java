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
    public static final RegistryObject<SoundEvent> PEEKER_IDLE =
            SOUNDS.register("peeker_idle", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "peeker_idle")));

    public static final RegistryObject<SoundEvent> PEEKER_HURT =
            SOUNDS.register("peeker_hurt", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "peeker_hurt")));

    public static final RegistryObject<SoundEvent> PEEKER_EXPLODE =
            SOUNDS.register("peeker_explode", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "peeker_explode")));
    public static final RegistryObject<SoundEvent> BABY_PEEKER_IDLE =
            SOUNDS.register("baby_peeker_idle", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "baby_peeker_idle")));

    public static final RegistryObject<SoundEvent> BABY_PEEKER_HURT =
            SOUNDS.register("baby_peeker_hurt", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "baby_peeker_hurt")));
    public static final RegistryObject<SoundEvent> MANDRAKE_CRY_1 =
            SOUNDS.register("mandrake_cry_1", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "mandrake_cry_1")));

    public static final RegistryObject<SoundEvent> MANDRAKE_CRY_2 =
            SOUNDS.register("mandrake_cry_2", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "mandrake_cry_2")));

    public static final RegistryObject<SoundEvent> MANDRAKE_CRY_3 =
            SOUNDS.register("mandrake_cry_3", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "mandrake_cry_3")));

    public static final RegistryObject<SoundEvent> MANDRAKE_CRY_4 =
            SOUNDS.register("mandrake_cry_4", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "mandrake_cry_4")));

    public static final RegistryObject<SoundEvent> MANDRAKE_CRY_5 =
            SOUNDS.register("mandrake_cry_5", () ->
                    SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "mandrake_cry_5")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}

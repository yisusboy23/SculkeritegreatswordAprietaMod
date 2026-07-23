package net.tucas.sculkeritegreatsword;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tucas.sculkeritegreatsword.item.Moditems;
import org.slf4j.Logger;
import net.tucas.sculkeritegreatsword.init.ModParticles;
import net.tucas.sculkeritegreatsword.init.ModEnchantments;
import net.tucas.sculkeritegreatsword.init.ModMobEffects;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.tucas.sculkeritegreatsword.client.renderer.SculkboomparticleRenderer;
import net.tucas.sculkeritegreatsword.init.ModSounds;
import net.tucas.sculkeritegreatsword.init.ModCreativeModeTabs;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.tucas.sculkeritegreatsword.events.ModBrewingRecipes;
import net.tucas.sculkeritegreatsword.init.ModPotions;

// The value here should match an entry in the META-INF/mods.toml file

@Mod(Sculkeritegreatsword.MOD_ID)
public class Sculkeritegreatsword
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "sculkeritegreatsword";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Sculkeritegreatsword()  {
        software.bernie.geckolib.GeckoLib.initialize();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        ModSounds.register(modEventBus);
        Moditems.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        modEventBus.addListener(ModBrewingRecipes::register);
        ModPotions.register(modEventBus);
        ModMobEffects.EFFECTS.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);


        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(net.tucas.sculkeritegreatsword.procedures.SculkGolemSpawnProcedure.class);

        modEventBus.addListener(this::addCreative);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    public static void queueServerWork(int ticks, Runnable action) {
        MinecraftForge.EVENT_BUS.register(new Object() {
            int wait = ticks;

            @SubscribeEvent
            public void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
                if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
                    if (--wait <= 0) {
                        action.run();
                        MinecraftForge.EVENT_BUS.unregister(this);
                    }
                }
            }
        });
    }

}

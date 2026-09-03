package net.tucas.sculkeritegreatsword.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.client.renderer.SculkGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.OxicopperGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.MushroomGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.SculkboomparticleRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.MushroomServantRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.LapisGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.GrindstoneGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.DiamondGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.ChorusGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.HydrantorGolemRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.DiamondFrostProjectileRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.KrillathanRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.BullsquamaRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.MudderRenderer;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.tucas.sculkeritegreatsword.client.renderer.DrillerRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.PeekerRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.MandrakeRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.ForgottenToyRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.ForgottenDroneRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SCULK_GOLEM.get(), SculkGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.OXICOPPER_GOLEM.get(), OxicopperGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSHROOM_GOLEM.get(), MushroomGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULKBOOMPARTICLE.get(), SculkboomparticleRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSHROOM_SERVANT.get(), MushroomServantRenderer::new);
        event.registerEntityRenderer(ModEntities.LAPIS_GOLEM.get(), LapisGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.GRINDSTONE_GOLEM.get(), GrindstoneGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.DIAMOND_GOLEM.get(), DiamondGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.CHORUS_GOLEM.get(), ChorusGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.DIAMOND_FROST_PROJECTILE.get(), DiamondFrostProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.HYDRANTOR_GOLEM.get(), HydrantorGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.DRILLER.get(), DrillerRenderer::new);
        event.registerEntityRenderer(ModEntities.KRILLATHAN.get(), KrillathanRenderer::new);
        event.registerEntityRenderer(ModEntities.KRILLATHAN_BABY.get(), KrillathanRenderer::new);
        event.registerEntityRenderer(ModEntities.BULLSQUAMA.get(), BullsquamaRenderer::new);
        event.registerEntityRenderer(ModEntities.MUDDER.get(), MudderRenderer::new);
        event.registerEntityRenderer(ModEntities.PEEKER.get(), PeekerRenderer::new);
        event.registerEntityRenderer(ModEntities.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(ModEntities.SPORES_SHROOMERS.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FORGOTTEN_TOY.get(), ForgottenToyRenderer::new);
        event.registerEntityRenderer(ModEntities.FORGOTTEN_DRONE.get(), ForgottenDroneRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANDRAKE_ROOT_BLOCK.get(), RenderType.cutout());
        });
    }
}
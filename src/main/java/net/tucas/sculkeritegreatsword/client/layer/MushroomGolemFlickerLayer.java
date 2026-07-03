package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MushroomGolemFlickerLayer extends GeoRenderLayer<MushroomGolemEntity> {

    private static final ResourceLocation FLICKER_TEXTURE = new ResourceLocation(
            Sculkeritegreatsword.MOD_ID, "textures/entity/mushroom_golem_flicker.png");

    public MushroomGolemFlickerLayer(GeoRenderer<MushroomGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, MushroomGolemEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        // Solo mostrar el flicker cuando está atacando
        if (!animatable.isGasReleasing()) {
            return;
        }

        RenderType flickerRenderType = RenderType.entityTranslucent(FLICKER_TEXTURE);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                flickerRenderType,
                bufferSource.getBuffer(flickerRenderType),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }
}
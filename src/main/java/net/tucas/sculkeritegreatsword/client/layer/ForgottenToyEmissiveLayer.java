package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenToyEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ForgottenToyEmissiveLayer extends GeoRenderLayer<ForgottenToyEntity> {

    private static final ResourceLocation GLOW_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/forgotten_toy_glow.png");
    private static final ResourceLocation OVERPOWERED_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/forgotten_toy_overpowered.png");

    public ForgottenToyEmissiveLayer(GeoRenderer<ForgottenToyEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, ForgottenToyEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        boolean ignited = animatable.isIgnited();
        boolean alert = animatable.isAlert();

        if (!ignited && !alert) {
            return;
        }

        ResourceLocation texture = ignited ? OVERPOWERED_TEXTURE : GLOW_TEXTURE;
        RenderType emissiveRenderType = RenderType.entityTranslucentEmissive(texture);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                emissiveRenderType,
                bufferSource.getBuffer(emissiveRenderType),
                partialTick,
                15728880,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }
}
package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenDroneEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ForgottenDroneEmissiveLayer extends GeoRenderLayer<ForgottenDroneEntity> {

    private static final ResourceLocation GLOW_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/forgotten_drone_glow.png");

    public ForgottenDroneEmissiveLayer(GeoRenderer<ForgottenDroneEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, ForgottenDroneEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        if (!animatable.isAlert()) {
            return; // sin glow si no te ve / no te ataca
        }

        RenderType emissiveRenderType = RenderType.entityTranslucentEmissive(GLOW_TEXTURE);

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
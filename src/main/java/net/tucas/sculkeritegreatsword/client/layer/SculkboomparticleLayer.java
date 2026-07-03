package net.tucas.sculkeritegreatsword.client.layer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.entity.SculkboomparticleEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SculkboomparticleLayer extends GeoRenderLayer<SculkboomparticleEntity> {
    private static final ResourceLocation GLOW = new ResourceLocation("sculkeritegreatsword", "textures/entity/sculkboom.png");

    public SculkboomparticleLayer(GeoRenderer<SculkboomparticleEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, SculkboomparticleEntity animatable, BakedGeoModel model,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        RenderType glowType = RenderType.eyes(GLOW);
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable,
                glowType, bufferSource.getBuffer(glowType), partialTick, packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
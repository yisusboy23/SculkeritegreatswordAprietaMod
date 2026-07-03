package net.tucas.sculkeritegreatsword.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.KrillathanEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class KrillathanGlowLayer extends GeoRenderLayer<KrillathanEntity> {

    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(
            Sculkeritegreatsword.MOD_ID, "textures/entity/krillathan_glow.png"
    );

    public KrillathanGlowLayer(GeoEntityRenderer<KrillathanEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, KrillathanEntity entity, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {

        if (entity.isBaby()) return;

        RenderType glowRenderType = RenderType.eyes(GLOW_TEXTURE);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        // Pulso suave tipo Warden
        float intensity = 0.8F + (entity.tickCount % 20) / 100.0F;

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                entity,
                glowRenderType,
                glowBuffer,
                partialTick,
                15728640,
                OverlayTexture.NO_OVERLAY,
                intensity, intensity, intensity, 1.0F
        );
    }
}
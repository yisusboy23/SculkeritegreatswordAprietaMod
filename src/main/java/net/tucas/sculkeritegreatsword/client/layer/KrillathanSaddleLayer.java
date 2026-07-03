package net.tucas.sculkeritegreatsword.client.layer;

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

public class KrillathanSaddleLayer extends GeoRenderLayer<KrillathanEntity> {

    private static final ResourceLocation SADDLE_TEXTURE = new ResourceLocation(
            Sculkeritegreatsword.MOD_ID, "textures/entity/krillathan_saddle.png");

    public KrillathanSaddleLayer(GeoEntityRenderer<KrillathanEntity> renderer) {
        super(renderer);
    }

    // Solo renderiza la saddle si la entidad la tiene equipada
    @Override
    public void render(PoseStack poseStack, KrillathanEntity entity, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {

        if (!entity.isSaddled()) return;

        RenderType saddleRenderType = RenderType.entityCutoutNoCull(SADDLE_TEXTURE);
        VertexConsumer saddleBuffer = bufferSource.getBuffer(saddleRenderType);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, entity,
                saddleRenderType, saddleBuffer,
                partialTick, packedLight,
                OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f);
    }
}
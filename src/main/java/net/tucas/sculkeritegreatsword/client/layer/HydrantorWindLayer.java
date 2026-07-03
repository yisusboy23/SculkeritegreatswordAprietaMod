package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.HydrantorGolemEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HydrantorWindLayer extends GeoRenderLayer<HydrantorGolemEntity> {

    private static final ResourceLocation WIND_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/wind.png");

    public HydrantorWindLayer(GeoRenderer<HydrantorGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, HydrantorGolemEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        // Solo renderizar la capa de viento cuando está usando Wind Attack
        if (!animatable.isUsingWindAttack()) {
            return;
        }

        // Renderizar con luz máxima (fullbright) y sin overlay de daño
        RenderType windRenderType = RenderType.eyes(WIND_TEXTURE);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                windRenderType,
                bufferSource.getBuffer(windRenderType),
                partialTick,
                15728880, // Luz máxima
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }
}
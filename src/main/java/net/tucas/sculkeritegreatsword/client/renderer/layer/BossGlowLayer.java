package net.tucas.sculkeritegreatsword.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Predicate;

/** Capa emisiva (full-bright) que solo se dibuja cuando la condición se cumple. */
public class BossGlowLayer extends GeoRenderLayer<ForgottenConstructEntity> {
    private final ResourceLocation texture;
    private final Predicate<ForgottenConstructEntity> condition;

    public BossGlowLayer(GeoRenderer<ForgottenConstructEntity> renderer, ResourceLocation texture,
                         Predicate<ForgottenConstructEntity> condition) {
        super(renderer);
        this.texture = texture;
        this.condition = condition;
    }

    @Override
    public void render(PoseStack poseStack, ForgottenConstructEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (!condition.test(animatable)) return;
        RenderType glow = RenderType.eyes(texture);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow,
                bufferSource.getBuffer(glow), partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}

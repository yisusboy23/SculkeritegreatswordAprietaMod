package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.client.layer.SculkGolemAttackLayer;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.SculkGolemModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkGolemRenderer extends GeoEntityRenderer<SculkGolemEntity> {

    public SculkGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkGolemModel());
        this.shadowRadius = 0.9f;

        // ⚡ AÑADIR LA CAPA DE ATAQUE QUE SE RENDERIZA ENCIMA
        this.addRenderLayer(new SculkGolemAttackLayer(this));
    }

    @Override
    public RenderType getRenderType(SculkGolemEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, SculkGolemEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {

        // Escalar el golem
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(SculkGolemEntity entityLivingBaseIn) {
        return 90.0f;
    }
}
package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.client.layer.HydrantorWindLayer;
import net.tucas.sculkeritegreatsword.entity.custom.HydrantorGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.HydrantorGolemModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HydrantorGolemRenderer extends GeoEntityRenderer<HydrantorGolemEntity> {

    public HydrantorGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HydrantorGolemModel());
        this.shadowRadius = 1.0f;

        // Añadir la capa de viento que se renderiza encima durante el ataque
        this.addRenderLayer(new HydrantorWindLayer(this));
    }

    @Override
    public RenderType getRenderType(HydrantorGolemEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, HydrantorGolemEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {

        // Escalar el golem
        this.scaleHeight = 1.6f;
        this.scaleWidth = 1.6f;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(HydrantorGolemEntity entityLivingBaseIn) {
        return 90.0f;
    }
}
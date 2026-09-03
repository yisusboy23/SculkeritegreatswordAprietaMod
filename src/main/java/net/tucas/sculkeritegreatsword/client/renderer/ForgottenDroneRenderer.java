package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.client.layer.ForgottenDroneEmissiveLayer;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenDroneEntity;
import net.tucas.sculkeritegreatsword.entity.model.ForgottenDroneModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ForgottenDroneRenderer extends GeoEntityRenderer<ForgottenDroneEntity> {
    public ForgottenDroneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ForgottenDroneModel());
        this.shadowRadius = 0.4F;

        this.addRenderLayer(new ForgottenDroneEmissiveLayer(this));
    }

    @Override
    public void preRender(PoseStack poseStack, ForgottenDroneEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
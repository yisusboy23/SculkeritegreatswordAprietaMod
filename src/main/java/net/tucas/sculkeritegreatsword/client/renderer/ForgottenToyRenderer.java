package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.client.layer.ForgottenToyEmissiveLayer;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenToyEntity;
import net.tucas.sculkeritegreatsword.entity.model.ForgottenToyModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ForgottenToyRenderer extends GeoEntityRenderer<ForgottenToyEntity> {
    public ForgottenToyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ForgottenToyModel());
        this.shadowRadius = 0.4F;

        this.addRenderLayer(new ForgottenToyEmissiveLayer(this));
    }

    @Override
    public void preRender(PoseStack poseStack, ForgottenToyEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        this.scaleHeight = 0.6f;
        this.scaleWidth = 0.6f;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
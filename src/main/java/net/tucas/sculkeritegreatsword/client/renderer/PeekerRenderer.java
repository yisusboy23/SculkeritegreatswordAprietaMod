package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.PeekerEntity;
import net.tucas.sculkeritegreatsword.entity.model.PeekerModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PeekerRenderer extends GeoEntityRenderer<PeekerEntity> {

    private static final float SCALE = 1.4F;

    public PeekerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PeekerModel());
        this.shadowRadius = 0.7F;
    }

    @Override
    public void preRender(PoseStack poseStack, PeekerEntity entity, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = entity.isBaby() ? SCALE * 0.5F : SCALE;
        this.scaleWidth = scale;
        this.scaleHeight = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
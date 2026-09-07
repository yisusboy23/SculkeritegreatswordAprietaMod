package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.WoolodonEntity;
import net.tucas.sculkeritegreatsword.entity.model.WoolodonModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WoolodonRenderer extends GeoEntityRenderer<WoolodonEntity> {

    public WoolodonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WoolodonModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(WoolodonEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        if (entity.isBaby()) {
            // Cambia estos valores si quieres al bebé más grande (ej: 0.7f) o más pequeño (ej: 0.3f)
            float babyScale = 1.5f;
            poseStack.scale(babyScale, babyScale, babyScale);
        } else {
            // Cambia estos valores si quieres al adulto más grande (ej: 1.5f)
            float adultScale = 3.0f;
            poseStack.scale(adultScale, adultScale, adultScale);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }
}
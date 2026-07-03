package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.client.layer.MushroomGolemFlickerLayer;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.MushroomGolemModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MushroomGolemRenderer extends GeoEntityRenderer<MushroomGolemEntity> {

    public MushroomGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MushroomGolemModel());
        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;

        // Agregar la capa de flicker cuando ataca
        this.addRenderLayer(new MushroomGolemFlickerLayer(this));
    }

    @Override
    public RenderType getRenderType(MushroomGolemEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, MushroomGolemEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {

        // Tamaño del golem (puedes ajustarlo)
        this.scaleHeight = 1.2f;
        this.scaleWidth = 1.2f;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(MushroomGolemEntity entityLivingBaseIn) {
        return 0.0f;
    }
}
package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.client.layer.OxicopperGolemEmissiveLayer;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.OxicopperGolemModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OxicopperGolemRenderer extends GeoEntityRenderer<OxicopperGolemEntity> {

    public OxicopperGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OxicopperGolemModel());
        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;

        // ⚡ AÑADIR LA CAPA EMISIVA QUE SE RENDERIZA ENCIMA
        this.addRenderLayer(new OxicopperGolemEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(OxicopperGolemEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, OxicopperGolemEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {

        this.scaleHeight = 1.6f;
        this.scaleWidth = 1.6f;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(OxicopperGolemEntity entityLivingBaseIn) {
        return 0.0f;
    }
}
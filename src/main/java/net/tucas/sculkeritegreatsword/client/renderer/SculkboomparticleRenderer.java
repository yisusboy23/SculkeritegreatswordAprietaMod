package net.tucas.sculkeritegreatsword.client.renderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.tucas.sculkeritegreatsword.client.layer.SculkboomparticleLayer;
import net.tucas.sculkeritegreatsword.client.model.SculkboomparticleModel;
import net.tucas.sculkeritegreatsword.entity.SculkboomparticleEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

public class SculkboomparticleRenderer extends GeoEntityRenderer<SculkboomparticleEntity> {

    public SculkboomparticleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkboomparticleModel());
        this.scaleWidth = 1.8f;
        this.scaleHeight = 1.8f;
        this.addRenderLayer(new SculkboomparticleLayer(this));
    }

    @Override
    public RenderType getRenderType(SculkboomparticleEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, SculkboomparticleEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        this.scaleHeight = 1.8f;
        this.scaleWidth = 1.8f;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(SculkboomparticleEntity entityLivingBaseIn) {
        return 0.0f;
    }
}
package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ResonarchShieldLayer extends GeoRenderLayer<ResonarchEntity> {

    private static final ResourceLocation SHIELD_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/wither/wither_armor.png");

    public ResonarchShieldLayer(GeoRenderer<ResonarchEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, ResonarchEntity animatable, BakedGeoModel bakedModel,
                        RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                        float partialTick, int packedLight, int packedOverlay) {

        if (!animatable.isShieldActive()) return;

        poseStack.pushPose();
        // Capa ligeramente más grande que el cuerpo, como el aura del Wither
        poseStack.scale(1.08F, 1.08F, 1.08F);

        RenderType shieldRenderType = RenderType.entityTranslucentEmissive(SHIELD_TEXTURE, false);
        VertexConsumer shieldBuffer = bufferSource.getBuffer(shieldRenderType);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, shieldRenderType,
                shieldBuffer, partialTick, packedLight, LightTexture.FULL_BRIGHT,
                1.0F, 1.0F, 1.0F, 0.6F);

        poseStack.popPose();
    }
}

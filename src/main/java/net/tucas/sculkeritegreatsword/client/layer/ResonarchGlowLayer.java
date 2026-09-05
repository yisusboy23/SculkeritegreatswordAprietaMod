package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ResonarchGlowLayer extends GeoRenderLayer<ResonarchEntity> {

    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(
            Sculkeritegreatsword.MOD_ID, "textures/entity/resonarch_glow.png");

    public ResonarchGlowLayer(GeoRenderer<ResonarchEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, ResonarchEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        // Cálculo de onda senoidal fluida para la pulsación suave estilo Warden
        float time = (animatable.tickCount + partialTick) * 0.1F;

        // Oscila suavemente entre 0.15 (brillo mínimo) y 1.0 (brillo máximo)
        float alpha = (Mth.sin(time) + 1.0F) * 0.425F + 0.15F;

        RenderType glowRenderType = RenderType.entityTranslucentEmissive(GLOW_TEXTURE, false);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                glowBuffer,
                partialTick,
                LightTexture.FULL_BRIGHT, // Luz de cueva ignorada (brillo propio)
                packedOverlay,
                1.0F, 1.0F, 1.0F, alpha // Aplica la transparencia animada
        );
    }
}
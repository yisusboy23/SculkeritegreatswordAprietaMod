package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class MushroomServantEmissiveLayer extends GeoRenderLayer<MushroomServantEntity> {

    public MushroomServantEmissiveLayer(GeoRenderer<MushroomServantEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, MushroomServantEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        int explosionState = animatable.getExplosionState();

        // Solo renderizar si está en estado de explosión
        if (explosionState == 0) {
            return;
        }

        // Obtener la textura correcta según variante y estado de explosión
        ResourceLocation emissiveTexture = getEmissiveTexture(animatable);

        // Renderizar con luz máxima (fullbright) para efecto brillante
        RenderType emissiveRenderType = RenderType.eyes(emissiveTexture);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                emissiveRenderType,
                bufferSource.getBuffer(emissiveRenderType),
                partialTick,
                15728880, // Luz máxima (fullbright)
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }

    private ResourceLocation getEmissiveTexture(MushroomServantEntity animatable) {
        int explosionState = animatable.getExplosionState();
        int variant = animatable.getVariant();

        if (variant == 0) {
            // Moycano
            if (explosionState == 1) {
                return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/moycano_explosion_1.png");
            } else {
                return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/moycano_explosion_2.png");
            }
        } else {
            // Ferdinand
            if (explosionState == 1) {
                return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/ferdinand_explosion_1.png");
            } else {
                return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/ferdinand_explosion_2.png");
            }
        }
    }
}
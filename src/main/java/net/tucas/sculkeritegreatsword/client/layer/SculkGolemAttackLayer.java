package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SculkGolemAttackLayer extends GeoRenderLayer<SculkGolemEntity> {

    private static final ResourceLocation ATTACK_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/sculk_golem_attack.png");

    public SculkGolemAttackLayer(GeoRenderer<SculkGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, SculkGolemEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        // Solo renderizar la capa de ataque cuando está usando Sonic Boom
        if (!animatable.isUsingSonicBoom()) {
            return;
        }

        // Renderizar con luz máxima (fullbright) y sin overlay de daño
        RenderType attackRenderType = RenderType.eyes(ATTACK_TEXTURE);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                attackRenderType,
                bufferSource.getBuffer(attackRenderType),
                partialTick,
                15728880, // Luz máxima
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }
}
package net.tucas.sculkeritegreatsword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.LapisGolemEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class LapisGolemCrackLayer extends GeoRenderLayer<LapisGolemEntity> {

    private static final ResourceLocation CRACK1_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/lapis_golem_crack1.png");

    private static final ResourceLocation CRACK2_TEXTURE =
            new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/lapis_golem_crack2.png");

    public LapisGolemCrackLayer(GeoRenderer<LapisGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, LapisGolemEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        float health = animatable.getHealth();
        ResourceLocation crackTexture = getCrackTexture(health);

        // Si no hay grietas que mostrar, salir
        if (crackTexture == null) {
            return;
        }

        // Renderizar la capa de grietas
        RenderType crackRenderType = RenderType.entityCutoutNoCull(crackTexture);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                crackRenderType,
                bufferSource.getBuffer(crackRenderType),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
    }

    private ResourceLocation getCrackTexture(float health) {
        // 20 vida o menos = crack1
        if (health <= 20.0F && health > 10.0F) {
            return CRACK1_TEXTURE;
        }
        // 10 vida o menos = crack2
        else if (health <= 10.0F) {
            return CRACK2_TEXTURE;
        }
        // Más de 20 vida = sin grietas
        return null;
    }
}
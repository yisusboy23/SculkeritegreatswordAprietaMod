package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.client.renderer.layer.BeamLayer;
import net.tucas.sculkeritegreatsword.client.renderer.layer.BossGlowLayer;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;
import net.tucas.sculkeritegreatsword.entity.model.ForgottenConstructModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ForgottenConstructRenderer extends GeoEntityRenderer<ForgottenConstructEntity> {
    private static final ResourceLocation GLOW =
            new ResourceLocation(ForgottenConstructModel.MOD_ID, "textures/entity/forgotten_construct_glow.png");
    private static final ResourceLocation OVERPOWER =
            new ResourceLocation(ForgottenConstructModel.MOD_ID, "textures/entity/forgotten_construct_overpower.png");

    // Define el factor de escala (por ejemplo, 2.0F lo hace el doble de grande)
    private static final float SCALE = 1.8F;

    public ForgottenConstructRenderer(EntityRendererProvider.Context context) {
        super(context, new ForgottenConstructModel());
        this.shadowRadius = 0.5F * SCALE;
        addRenderLayer(new BossGlowLayer(this, GLOW, ForgottenConstructEntity::isGlowActive));
        addRenderLayer(new BossGlowLayer(this, OVERPOWER, ForgottenConstructEntity::isOverpowerActive));
        addRenderLayer(new BeamLayer(this));
    }

    @Override
    public void render(ForgottenConstructEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // Escala el modelo en los ejes X, Y, Z
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

}
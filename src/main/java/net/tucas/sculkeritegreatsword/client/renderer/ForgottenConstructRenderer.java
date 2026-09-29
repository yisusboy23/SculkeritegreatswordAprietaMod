package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.client.renderer.layer.BossGlowLayer; // Import del layer
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;
import net.tucas.sculkeritegreatsword.entity.model.ForgottenConstructModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ForgottenConstructRenderer extends GeoEntityRenderer<ForgottenConstructEntity> {
    private static final ResourceLocation GLOW =
            new ResourceLocation(ForgottenConstructModel.MOD_ID, "textures/entity/forgotten_construct_glow.png");
    private static final ResourceLocation OVERPOWER =
            new ResourceLocation(ForgottenConstructModel.MOD_ID, "textures/entity/forgotten_construct_overpower.png");

    public ForgottenConstructRenderer(EntityRendererProvider.Context context) {
        super(context, new ForgottenConstructModel());
        this.shadowRadius = 2.0F;
        addRenderLayer(new BossGlowLayer(this, GLOW, ForgottenConstructEntity::isGlowActive));
        addRenderLayer(new BossGlowLayer(this, OVERPOWER, ForgottenConstructEntity::isOverpowerActive));
    }
}
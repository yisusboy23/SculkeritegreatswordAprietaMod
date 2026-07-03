package net.tucas.sculkeritegreatsword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;
import net.tucas.sculkeritegreatsword.entity.model.MudderModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MudderRenderer extends GeoEntityRenderer<MudderEntity> {

    public MudderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MudderModel());
        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;
    }
}
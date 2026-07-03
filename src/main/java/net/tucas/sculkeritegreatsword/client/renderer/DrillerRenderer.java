package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.DrillerEntity;
import net.tucas.sculkeritegreatsword.entity.model.DrillerModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DrillerRenderer extends GeoEntityRenderer<DrillerEntity> {

    public DrillerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DrillerModel());
        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;
    }
}
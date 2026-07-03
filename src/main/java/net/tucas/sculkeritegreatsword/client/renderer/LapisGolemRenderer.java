package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.client.layer.LapisGolemCrackLayer;
import net.tucas.sculkeritegreatsword.entity.custom.LapisGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.LapisGolemModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LapisGolemRenderer extends GeoEntityRenderer<LapisGolemEntity> {

    public LapisGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LapisGolemModel());

        // AÑADIR LAYER DE GRIETAS
        this.addRenderLayer(new LapisGolemCrackLayer(this));

        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;
    }
}
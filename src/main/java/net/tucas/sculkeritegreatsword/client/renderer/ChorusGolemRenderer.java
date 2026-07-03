package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.ChorusGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.ChorusGolemModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ChorusGolemRenderer extends GeoEntityRenderer<ChorusGolemEntity> {

    public ChorusGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChorusGolemModel());

        // Ajusta la escala si es necesario
        this.scaleWidth = 2.0f;
        this.scaleHeight = 2.0f;
    }
}
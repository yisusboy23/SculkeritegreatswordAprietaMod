package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.DiamondGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.DiamondGolemModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DiamondGolemRenderer extends GeoEntityRenderer<DiamondGolemEntity> {

    public DiamondGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DiamondGolemModel());

        // Ajusta la escala si es necesario
        this.scaleWidth = 1.3f;
        this.scaleHeight = 1.3f;
    }
}
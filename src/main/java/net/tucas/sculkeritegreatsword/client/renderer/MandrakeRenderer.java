package net.tucas.sculkeritegreatsword.client.renderer;

import net.tucas.sculkeritegreatsword.entity.custom.MandrakeEntity;
import net.tucas.sculkeritegreatsword.entity.model.MandrakeModel; // <-- este import faltaba/estaba mal
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MandrakeRenderer extends GeoEntityRenderer<MandrakeEntity> {
    public MandrakeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MandrakeModel());
    }
}
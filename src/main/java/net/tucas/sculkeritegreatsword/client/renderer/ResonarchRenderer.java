package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;
import net.tucas.sculkeritegreatsword.entity.model.ResonarchModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ResonarchRenderer extends GeoEntityRenderer<ResonarchEntity> {

    public ResonarchRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ResonarchModel());
        this.addRenderLayer(new ResonarchShieldLayer(this));
    }
}

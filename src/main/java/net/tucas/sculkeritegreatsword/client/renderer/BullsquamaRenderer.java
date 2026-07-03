package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.BullsquamaEntity;
import net.tucas.sculkeritegreatsword.entity.model.BullsquamaModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BullsquamaRenderer extends GeoEntityRenderer<BullsquamaEntity> {

    public BullsquamaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BullsquamaModel());
    }
}
package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.client.layer.MushroomServantEmissiveLayer;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;
import net.tucas.sculkeritegreatsword.entity.model.MushroomServantModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MushroomServantRenderer extends GeoEntityRenderer<MushroomServantEntity> {

    public MushroomServantRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MushroomServantModel());
        this.scaleWidth = 0.6f;
        this.scaleHeight = 0.6f;

        // Agregar la capa emisiva para que brille cuando esté explotando
        this.addRenderLayer(new MushroomServantEmissiveLayer(this));
    }
}
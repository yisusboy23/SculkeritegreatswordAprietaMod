package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.GrindstoneGolemEntity;
import net.tucas.sculkeritegreatsword.entity.model.GrindstoneGolemModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GrindstoneGolemRenderer extends GeoEntityRenderer<GrindstoneGolemEntity> {

    public GrindstoneGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GrindstoneGolemModel());

        // Ajusta la escala si es necesario
        this.scaleWidth = 1.3f;
        this.scaleHeight = 1.3f;
    }
}
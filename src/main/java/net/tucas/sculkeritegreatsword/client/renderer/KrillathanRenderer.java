package net.tucas.sculkeritegreatsword.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.tucas.sculkeritegreatsword.entity.custom.KrillathanEntity;
import net.tucas.sculkeritegreatsword.entity.model.KrillathanModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.tucas.sculkeritegreatsword.client.renderer.layer.KrillathanGlowLayer;
import net.tucas.sculkeritegreatsword.client.layer.KrillathanSaddleLayer;

public class KrillathanRenderer extends GeoEntityRenderer<KrillathanEntity> {

    public KrillathanRenderer(EntityRendererProvider.Context context) {
        super(context, new KrillathanModel());
        this.addRenderLayer(new KrillathanGlowLayer(this));
        this.addRenderLayer(new KrillathanSaddleLayer(this));
    }
}

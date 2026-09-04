package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ResonarchEntity;
import software.bernie.geckolib.model.GeoModel;

public class ResonarchModel extends GeoModel<ResonarchEntity> {

    @Override
    public ResourceLocation getModelResource(ResonarchEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/resonarch.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ResonarchEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/resonarch.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ResonarchEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/resonarch.animation.json");
    }
}

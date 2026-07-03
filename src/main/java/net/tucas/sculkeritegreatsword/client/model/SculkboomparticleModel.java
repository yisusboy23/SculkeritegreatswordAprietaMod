package net.tucas.sculkeritegreatsword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.entity.SculkboomparticleEntity;
import software.bernie.geckolib.model.GeoModel;

public class SculkboomparticleModel extends GeoModel<SculkboomparticleEntity> {
    @Override
    public ResourceLocation getModelResource(SculkboomparticleEntity entity) {
        return new ResourceLocation("sculkeritegreatsword", "geo/sculk_boom.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SculkboomparticleEntity entity) {
        return new ResourceLocation("sculkeritegreatsword", "textures/entity/" + entity.getTexture() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SculkboomparticleEntity entity) {
        return new ResourceLocation("sculkeritegreatsword", "animations/sculk_boom.animation.json");
    }
}
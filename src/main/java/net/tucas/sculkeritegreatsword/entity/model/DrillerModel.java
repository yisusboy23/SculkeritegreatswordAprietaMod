package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.DrillerEntity;
import software.bernie.geckolib.model.GeoModel;

public class DrillerModel extends GeoModel<DrillerEntity> {

    @Override
    public ResourceLocation getModelResource(DrillerEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/driller_baby.geo.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/driller.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DrillerEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/driller_baby.png");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/driller.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DrillerEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/driller_baby.animation.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/driller.animation.json");
    }
}
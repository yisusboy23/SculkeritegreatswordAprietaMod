package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;
import software.bernie.geckolib.model.GeoModel;

public class MudderModel extends GeoModel<MudderEntity> {

    @Override
    public ResourceLocation getModelResource(MudderEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/mudder_baby.geo.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/mudder.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MudderEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/muddler_baby.png");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/mudder.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MudderEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/mudder_baby.animation.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/mudder.animation.json");
    }
}
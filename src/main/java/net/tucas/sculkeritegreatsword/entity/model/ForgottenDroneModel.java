package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenDroneEntity;
import software.bernie.geckolib.model.GeoModel;

public class ForgottenDroneModel extends GeoModel<ForgottenDroneEntity> {

    @Override
    public ResourceLocation getModelResource(ForgottenDroneEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/forgotten_drone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ForgottenDroneEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/forgotten_drone.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ForgottenDroneEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/forgotten_drone.animation.json");
    }
}
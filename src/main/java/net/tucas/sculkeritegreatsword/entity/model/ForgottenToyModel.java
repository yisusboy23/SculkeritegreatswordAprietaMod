package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenToyEntity;
import software.bernie.geckolib.model.GeoModel;

public class ForgottenToyModel extends GeoModel<ForgottenToyEntity> {

    @Override
    public ResourceLocation getModelResource(ForgottenToyEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/forgotten_toy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ForgottenToyEntity animatable) {
        // SIEMPRE la base fija, sin ternarias (?:) ni condicionales
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/forgotten_toy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ForgottenToyEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/forgotten_toy.animation.json");
    }
}
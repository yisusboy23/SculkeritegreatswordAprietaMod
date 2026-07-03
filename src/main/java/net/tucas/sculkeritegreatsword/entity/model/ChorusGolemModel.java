package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.ChorusGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class ChorusGolemModel extends GeoModel<ChorusGolemEntity> {

    @Override
    public ResourceLocation getModelResource(ChorusGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/chorus_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ChorusGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/chorus_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ChorusGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/chorus_golem.animation.json");
    }
}
package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.GrindstoneGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class GrindstoneGolemModel extends GeoModel<GrindstoneGolemEntity> {

    @Override
    public ResourceLocation getModelResource(GrindstoneGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/grindstone_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GrindstoneGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/grindstone_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GrindstoneGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/grindstone_golem.animation.json");
    }
}
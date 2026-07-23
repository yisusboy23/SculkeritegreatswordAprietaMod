package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.MandrakeEntity;
import software.bernie.geckolib.model.GeoModel;

public class MandrakeModel extends GeoModel<MandrakeEntity> {

    @Override
    public ResourceLocation getModelResource(MandrakeEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/mandrake.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MandrakeEntity animatable) {
        if (animatable.isCrying()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/block/mandrake_root_crying.png");
        }
        if (animatable.isAsleep()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/block/mandrake_root_sleeping.png");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/block/mandrake_root.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MandrakeEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/mandrake.animation.json");
    }
}
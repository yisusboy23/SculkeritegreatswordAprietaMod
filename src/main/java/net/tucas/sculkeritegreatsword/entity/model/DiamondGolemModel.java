package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.DiamondGolemEntity;
import software.bernie.geckolib.model.GeoModel;

public class DiamondGolemModel extends GeoModel<DiamondGolemEntity> {

    @Override
    public ResourceLocation getModelResource(DiamondGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/diamond_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DiamondGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/diamond_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DiamondGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/diamond_golem.animation.json");
    }
}
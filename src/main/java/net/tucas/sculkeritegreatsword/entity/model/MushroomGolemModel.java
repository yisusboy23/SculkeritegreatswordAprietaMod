package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;

public class MushroomGolemModel extends GeoModel<MushroomGolemEntity> {

    @Override
    public ResourceLocation getModelResource(MushroomGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/mushroom_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MushroomGolemEntity animatable) {
        // Cambiar textura según la variante del golem
        return switch (animatable.getVariant()) {
            case BROWN -> new ResourceLocation(Sculkeritegreatsword.MOD_ID,
                    "textures/entity/mushroom_golem_brown.png");
            case RED -> new ResourceLocation(Sculkeritegreatsword.MOD_ID,
                    "textures/entity/mushroom_golem.png");
        };
    }

    @Override
    public ResourceLocation getAnimationResource(MushroomGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/mushroom_golem.animation.json");
    }
}
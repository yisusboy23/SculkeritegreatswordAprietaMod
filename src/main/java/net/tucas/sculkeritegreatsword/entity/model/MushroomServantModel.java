package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;

public class MushroomServantModel extends GeoModel<MushroomServantEntity> {

    @Override
    public ResourceLocation getModelResource(MushroomServantEntity animatable) {
        if (animatable.getVariant() == 0) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/moycano.geo.json");
        } else {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/ferdinand.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(MushroomServantEntity animatable) {
        // SIEMPRE devolver la textura BASE
        // La EmissiveLayer se encarga de renderizar las explosiones encima
        if (animatable.getVariant() == 0) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/moycano.png");
        } else {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/ferdinand.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(MushroomServantEntity animatable) {
        if (animatable.getVariant() == 0) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/moycano.animation.json");
        } else {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/ferdinand.animation.json");
        }
    }
}
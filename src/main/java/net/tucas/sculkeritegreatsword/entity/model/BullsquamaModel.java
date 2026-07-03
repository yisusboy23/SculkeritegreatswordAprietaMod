package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.BullsquamaEntity;
import software.bernie.geckolib.model.GeoModel;

public class BullsquamaModel extends GeoModel<BullsquamaEntity> {

    @Override
    public ResourceLocation getModelResource(BullsquamaEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/baby_bullsquama.geo.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/bullsquama.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BullsquamaEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/baby_bullsquama.png");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/bullsquama.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BullsquamaEntity entity) {
        if (entity.isBaby()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/baby_bullsquama.animation.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/bullsquama.animation.json");
    }
}
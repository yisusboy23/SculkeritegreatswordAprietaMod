package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.KrillathanEntity;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.init.ModEntities;

public class KrillathanModel extends GeoModel<KrillathanEntity> {

    @Override
    public ResourceLocation getModelResource(KrillathanEntity entity) {
        if (entity.getType() == ModEntities.KRILLATHAN_BABY.get()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/krillathan_baby.geo.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/krillathan.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KrillathanEntity entity) {
        if (entity.getType() == ModEntities.KRILLATHAN_BABY.get()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/krillathan_baby.png");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/krillathan.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KrillathanEntity entity) {
        if (entity.getType() == ModEntities.KRILLATHAN_BABY.get()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/krillathan_baby.animation.json");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/krillathan.animation.json");
    }
}
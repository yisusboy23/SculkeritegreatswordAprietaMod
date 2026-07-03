package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.HydrantorGolemEntity;

public class HydrantorGolemModel extends GeoModel<HydrantorGolemEntity> {

    @Override
    public ResourceLocation getModelResource(HydrantorGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/hydrantor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HydrantorGolemEntity animatable) {
        // Textura base normal
        // La textura de viento se renderiza como capa superpuesta
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/hydrantor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HydrantorGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/hydrantor.animation.json");
    }
}
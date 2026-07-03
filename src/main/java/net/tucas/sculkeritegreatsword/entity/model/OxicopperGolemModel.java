package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;

public class OxicopperGolemModel extends GeoModel<OxicopperGolemEntity> {

    @Override
    public ResourceLocation getModelResource(OxicopperGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/oxicopper_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OxicopperGolemEntity animatable) {
        // SIEMPRE retornar la textura normal como base
        // La textura emisiva se renderiza automáticamente como CAPA SUPERPUESTA
        // por el OxicopperGolemEmissiveLayer
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/oxicopper_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OxicopperGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/oxicopper_golem.animation.json");
    }
}
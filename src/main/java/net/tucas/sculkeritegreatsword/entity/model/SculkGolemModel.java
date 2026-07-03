package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;

public class SculkGolemModel extends GeoModel<SculkGolemEntity> {

    @Override
    public ResourceLocation getModelResource(SculkGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/sculk_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SculkGolemEntity animatable) {
        // SIEMPRE retornar la textura normal como base
        // La textura de ataque se renderiza automáticamente como CAPA SUPERPUESTA
        // por el SculkGolemAttackLayer
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/sculk_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SculkGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/sculk_golem.animation.json");
    }
}
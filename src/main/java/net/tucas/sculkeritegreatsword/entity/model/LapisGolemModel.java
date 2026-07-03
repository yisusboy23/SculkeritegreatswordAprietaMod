package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.LapisGolemEntity;

public class LapisGolemModel extends GeoModel<LapisGolemEntity> {

    @Override
    public ResourceLocation getModelResource(LapisGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/lapis_golem.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LapisGolemEntity animatable) {
        // Mostrar textura de ataque durante toda la animación
        if (animatable.isAttacking()) {
            return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/lapis_golem_attack.png");
        }
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/lapis_golem.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LapisGolemEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/lapis_golem.animation.json");
    }
}
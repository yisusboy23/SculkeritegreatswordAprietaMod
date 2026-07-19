package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.PeekerEntity;
import software.bernie.geckolib.model.GeoModel;

public class PeekerModel extends GeoModel<PeekerEntity> {

    @Override
    public ResourceLocation getModelResource(PeekerEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "geo/peeker.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PeekerEntity animatable) {
        // getTexture() ya refleja el estado (salvaje / ensillado / explotando) solo,
        // porque PeekerEntity la actualiza en setSaddled()/die(). No hace falta lógica aquí.
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "textures/entity/" + animatable.getTexture() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(PeekerEntity animatable) {
        return new ResourceLocation(Sculkeritegreatsword.MOD_ID, "animations/peeker.animation.json");
    }
}
package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.WoolodonEntity;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;

public class WoolodonModel extends GeoModel<WoolodonEntity> {

    @Override
    public ResourceLocation getModelResource(
            WoolodonEntity entity
    ) {

        if (entity.isBaby()) {
            return new ResourceLocation(
                    Sculkeritegreatsword.MOD_ID,
                    "geo/woolodon_baby.geo.json"
            );
        }

        return new ResourceLocation(
                Sculkeritegreatsword.MOD_ID,
                "geo/woolodon.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(
            WoolodonEntity entity
    ) {

        if (entity.isBaby()) {
            return new ResourceLocation(
                    Sculkeritegreatsword.MOD_ID,
                    "textures/entity/woolodon_baby.png"
            );
        }

        return new ResourceLocation(
                Sculkeritegreatsword.MOD_ID,
                "textures/entity/woolodon.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(
            WoolodonEntity entity
    ) {

        if (entity.isBaby()) {
            return new ResourceLocation(
                    Sculkeritegreatsword.MOD_ID,
                    "animations/woolodon_baby.animation.json"
            );
        }

        return new ResourceLocation(
                Sculkeritegreatsword.MOD_ID,
                "animations/woolodon.animation.json"
        );
    }

    @Override
    public void setCustomAnimations(
            WoolodonEntity entity,
            long instanceId,
            AnimationState<WoolodonEntity> animationState
    ) {

        super.setCustomAnimations(
                entity,
                instanceId,
                animationState
        );

        /*
         * El bebé utiliza otro modelo y no tiene
         * las partes de lana del adulto.
         */
        if (entity.isBaby()) {
            return;
        }

        /*
         * lana
         */
        this.getBone("lana").ifPresent(
                bone -> bone.setHidden(
                        entity.isWoolPartCut(0)
                )
        );

        /*
         * lana2
         */
        this.getBone("lana2").ifPresent(
                bone -> bone.setHidden(
                        entity.isWoolPartCut(1)
                )
        );

        /*
         * lana3
         */
        this.getBone("lana3").ifPresent(
                bone -> bone.setHidden(
                        entity.isWoolPartCut(2)
                )
        );

        /*
         * lana4
         */
        this.getBone("lana4").ifPresent(
                bone -> bone.setHidden(
                        entity.isWoolPartCut(3)
                )
        );

        /*
         * lana5
         */
        this.getBone("lana5").ifPresent(
                bone -> bone.setHidden(
                        entity.isWoolPartCut(4)
                )
        );
    }
}
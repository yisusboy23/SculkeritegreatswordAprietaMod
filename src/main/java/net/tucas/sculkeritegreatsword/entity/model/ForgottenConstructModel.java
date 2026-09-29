package net.tucas.sculkeritegreatsword.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class ForgottenConstructModel extends GeoModel<ForgottenConstructEntity> {
    public static final String MOD_ID = "sculkeritegreatsword";
    /** Nombre del hueso de la cabeza en el .geo.json (cámbialo si es distinto). */
    private static final String HEAD_BONE = "head";

    @Override public ResourceLocation getModelResource(ForgottenConstructEntity e) {
        return new ResourceLocation(MOD_ID, "geo/forgotten_construct.geo.json");
    }
    @Override public ResourceLocation getTextureResource(ForgottenConstructEntity e) {
        return new ResourceLocation(MOD_ID, "textures/entity/forgotten_construct.png");
    }
    @Override public ResourceLocation getAnimationResource(ForgottenConstructEntity e) {
        return new ResourceLocation(MOD_ID, "animations/forgotten_construct.animation.json");
    }

    @Override
    public void setCustomAnimations(ForgottenConstructEntity entity, long instanceId, AnimationState<ForgottenConstructEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        BossState s = entity.getBossState();
        if (s == BossState.SLEEP || s == BossState.DAZED) return;
        CoreGeoBone head = getAnimationProcessor().getBone(HEAD_BONE);
        if (head == null) return;
        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(data.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}

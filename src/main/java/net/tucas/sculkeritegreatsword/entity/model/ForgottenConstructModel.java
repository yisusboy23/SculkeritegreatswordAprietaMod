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
    /** Hueso de la cabeza: solo sube y baja. */
    private static final String HEAD_BONE = "Cabeza";
    /** Hueso que gira el cuerpo entero hacia el objetivo. */
    private static final String TURRET_BONE = "CuerpoRotara";

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

        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone head = getAnimationProcessor().getBone(HEAD_BONE);
        if (head != null) head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);

        CoreGeoBone turret = getAnimationProcessor().getBone(TURRET_BONE);
        if (turret != null) turret.setRotY(data.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
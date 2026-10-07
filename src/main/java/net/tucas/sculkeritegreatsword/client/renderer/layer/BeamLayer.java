package net.tucas.sculkeritegreatsword.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BeamLayer extends GeoRenderLayer<ForgottenConstructEntity> {
    private static final ResourceLocation BEAM = new ResourceLocation("textures/entity/beacon_beam.png");

    public BeamLayer(GeoRenderer<ForgottenConstructEntity> renderer) { super(renderer); }

    @Override
    public void render(PoseStack pose, ForgottenConstructEntity e, BakedGeoModel model, RenderType rt,
                       MultiBufferSource buf, VertexConsumer vc, float pt, int light, int overlay) {
        if (!e.isBeamActive()) return;

        Vector3f sc = new Vector3f();
        pose.last().pose().getScale(sc);
        float inv = 1.0F / sc.x;

        float bodyYaw = Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot);
        float headYaw = Mth.rotLerp(pt, e.yHeadRotO, e.yHeadRot);
        Vec3 rel = e.locatorOffset(ForgottenConstructEntity.NUCLEO_OFFSET, headYaw);

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));
        pose.translate(rel.x * inv, rel.y * inv, rel.z * inv);
        pose.scale(inv, inv, inv);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - e.getBeamYaw()));
        pose.mulPose(Axis.XP.rotationDegrees(90.0F + e.getBeamPitch()));
        pose.translate(-0.5D, 0.0D, -0.5D);
        BeaconRenderer.renderBeaconBeam(pose, buf, BEAM, pt, 1.0F, e.level().getGameTime(),
                0, (int) Math.ceil(e.getBeamLen()), new float[]{1f, 1f, 1f}, 0.3F, 0.38F);
        pose.popPose();
    }
}
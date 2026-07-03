package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;

public class KrillathanPart extends PartEntity<KrillathanEntity> {

    public final KrillathanEntity parent;
    private final EntityDimensions dimensions;

    public KrillathanPart(KrillathanEntity parent, float width, float height) {
        super(parent);
        this.parent = parent;
        this.dimensions = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return parent == null ? dimensions : dimensions.scale(parent.getScale());
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (parent == null) return InteractionResult.PASS;
        return parent.interact(player, hand);
    }

    @Override
    public boolean save(@NotNull CompoundTag tag) { return false; }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean is(net.minecraft.world.entity.@NotNull Entity entity) {
        return this == entity || parent == entity;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && parent.hurt(source, amount);
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(2.0D, 1.0D, 2.0D);
    }

    @Override
    public boolean shouldBeSaved() { return false; }

    public void setPosCenteredY(Vec3 pos) {
        this.setPos(pos.x, pos.y - this.getBbHeight() * 0.5F, pos.z);
    }

    public Vec3 centeredPosition() {
        return this.position().add(0, this.getBbHeight() * 0.5F, 0);
    }
    @Override
    public boolean isAttackable() {
        return true;
    }


    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}
}
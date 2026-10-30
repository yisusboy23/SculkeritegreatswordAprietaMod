package net.tucas.sculkeritegreatsword.mixin;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.tucas.sculkeritegreatsword.util.FinesseHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityMixin {

    @Shadow @Nullable private ResourceLocation lootTable;
    @Shadow @Nullable private Direction hitDirection;

    @Unique private static final String FINESSE_TAG = "FinesseLootTable";
    @Unique @Nullable private ResourceLocation finesse$lootTable;

    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void finesse$captureLootTable(Player player, CallbackInfo ci) {
        if (this.lootTable != null) {
            this.finesse$lootTable = this.lootTable;
        }
    }

    @Inject(method = "brushingCompleted", at = @At("HEAD"))
    private void finesse$onBrushingCompleted(Player player, CallbackInfo ci) {
        ResourceLocation id = this.finesse$lootTable != null ? this.finesse$lootTable : this.lootTable;
        this.finesse$lootTable = null;
        if (id == null) return;
        FinesseHelper.rollExtraRewards((BrushableBlockEntity) (Object) this, player, id, this.hitDirection);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void finesse$save(CompoundTag tag, CallbackInfo ci) {
        if (this.finesse$lootTable != null) {
            tag.putString(FINESSE_TAG, this.finesse$lootTable.toString());
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void finesse$load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(FINESSE_TAG, Tag.TAG_STRING)) {
            this.finesse$lootTable = ResourceLocation.tryParse(tag.getString(FINESSE_TAG));
        }
    }
}
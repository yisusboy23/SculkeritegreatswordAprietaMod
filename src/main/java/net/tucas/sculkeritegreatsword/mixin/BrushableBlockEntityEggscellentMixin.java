package net.tucas.sculkeritegreatsword.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.tucas.sculkeritegreatsword.util.EggscellentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityEggscellentMixin {

    @Shadow @Nullable private ResourceLocation lootTable;
    @Shadow private ItemStack item;

    @Unique @Nullable private ResourceLocation eggscellent$pendingTable;

    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void eggscellent$head(Player player, CallbackInfo ci) {
        this.eggscellent$pendingTable = this.lootTable;
    }

    @Inject(method = "unpackLootTable", at = @At("TAIL"))
    private void eggscellent$tail(Player player, CallbackInfo ci) {
        ResourceLocation id = this.eggscellent$pendingTable;
        this.eggscellent$pendingTable = null;
        if (id == null || this.lootTable != null) return; // solo si se desempaquetó ahora

        BrushableBlockEntity self = (BrushableBlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) return;

        ItemStack improved = EggscellentHelper.improve(serverLevel, player, self.getBlockPos(), id, this.item);
        if (improved != this.item) {
            this.item = improved;
            self.setChanged();
        }
    }
}
package net.tucas.sculkeritegreatsword.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import net.tucas.sculkeritegreatsword.init.ModEntities;

public class MudderBucketItem extends MobBucketItem {
    public MudderBucketItem(Item.Properties properties) {
        super(
                ModEntities.MUDDER,
                () -> Fluids.WATER,
                () -> SoundEvents.BUCKET_EMPTY_FISH,
                properties
        );
    }
}
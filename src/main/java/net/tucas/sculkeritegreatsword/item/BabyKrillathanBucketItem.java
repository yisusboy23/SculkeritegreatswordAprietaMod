package net.tucas.sculkeritegreatsword.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import net.tucas.sculkeritegreatsword.init.ModEntities;

public class BabyKrillathanBucketItem extends MobBucketItem {
    public BabyKrillathanBucketItem(Item.Properties properties) {
        super(
                ModEntities.KRILLATHAN_BABY,
                () -> Fluids.WATER,
                () -> SoundEvents.BUCKET_EMPTY_FISH,
                properties
        );
    }
}
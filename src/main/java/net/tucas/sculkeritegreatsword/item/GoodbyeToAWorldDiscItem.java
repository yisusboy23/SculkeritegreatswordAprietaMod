package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.tucas.sculkeritegreatsword.init.ModSounds;

public class GoodbyeToAWorldDiscItem extends RecordItem {
    public GoodbyeToAWorldDiscItem() {
        super(15,
                ModSounds.MUSIC_DISC_GOODBYE_TO_A_WORLD.get(),
                new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                4800);
    }
}

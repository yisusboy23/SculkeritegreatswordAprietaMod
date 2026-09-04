package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

public class MandrakeRoot extends ItemNameBlockItem {

    // Atributos exactos de la Zanahoria Dorada (Golden Carrot)
    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(1.2F)
            .build();

    public MandrakeRoot(Block block, Properties properties) {
        super(block, properties.food(FOOD_PROPERTIES));
    }
}
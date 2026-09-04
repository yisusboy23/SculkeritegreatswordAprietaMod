package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class GoldenMandrakeItem extends Item {

    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(0.9F)
            .alwaysEat()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F)
            .build();

    public GoldenMandrakeItem(Properties properties) {
        super(properties.food(FOOD_PROPERTIES));
    }
}
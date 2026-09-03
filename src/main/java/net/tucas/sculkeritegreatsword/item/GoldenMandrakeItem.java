package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class GoldenMandrakeItem extends Item {

    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(0.9F)
            .alwaysEat()
            .build();

    public GoldenMandrakeItem(Properties properties) {
        super(properties.food(FOOD_PROPERTIES));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            // Regeneración II por 5 segundos (100 ticks)
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            // Absorción I por 2 minutos (2400 ticks)
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        }

        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false; // Sin efecto de brillo
    }
}
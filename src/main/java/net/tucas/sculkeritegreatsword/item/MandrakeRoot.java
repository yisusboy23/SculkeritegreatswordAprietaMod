package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MandrakeRoot extends Item {

    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.3F)
            .build();

    public MandrakeRoot(Properties properties) {
        super(properties.food(FOOD_PROPERTIES));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32; // igual que la manzana de oro
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player) {
            // Versión "menor" de la manzana de oro:
            // Regeneración I por 5s (la manzana de oro da Regeneración II)
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            // Absorción I por 1 min (la manzana de oro da 2 min)
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0));
        }

        return result;
    }
}
package net.tucas.sculkeritegreatsword.events;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.tucas.sculkeritegreatsword.init.ModPotions;
import net.tucas.sculkeritegreatsword.item.Moditems;

public class ModBrewingRecipes {

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Poción Incómoda + Mandrake Root Plant -> Poción del Canto de Mandrágora
            BrewingRecipeRegistry.addRecipe(
                    Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                    Ingredient.of(Moditems.MANDRAKE_ROOT.get()),
                    PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.MANDRAKE_SONG.get())
            );

            // Opcional: permitir convertirla en Arrojadiza (Splash)
            BrewingRecipeRegistry.addRecipe(
                    Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.MANDRAKE_SONG.get())),
                    Ingredient.of(Items.GUNPOWDER),
                    PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.MANDRAKE_SONG.get())
            );

            // Opcional: convertir la Arrojadiza en Persistente (Lingering)
            BrewingRecipeRegistry.addRecipe(
                    Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.MANDRAKE_SONG.get())),
                    Ingredient.of(Items.DRAGON_BREATH),
                    PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.MANDRAKE_SONG.get())
            );
        });
    }
}
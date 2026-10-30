package net.tucas.sculkeritegreatsword.init;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.enchantment.FastrechargeEnchantment;
import net.tucas.sculkeritegreatsword.enchantment.KineticenergyEnchantment;
import net.tucas.sculkeritegreatsword.enchantment.FinesseEnchantment;
import net.tucas.sculkeritegreatsword.enchantment.EggscellentEnchantment;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<Enchantment> FASTRECHARGE =
            ENCHANTMENTS.register("fastrecharge", FastrechargeEnchantment::new);

    public static final RegistryObject<Enchantment> KINETICENERGY =
            ENCHANTMENTS.register("kineticenergy", KineticenergyEnchantment::new);

    public static final RegistryObject<Enchantment> FINESSE =
            ENCHANTMENTS.register("finesse", FinesseEnchantment::new);

    public static final RegistryObject<Enchantment> EGGSCELLENT =
            ENCHANTMENTS.register("eggscellent", EggscellentEnchantment::new);
}
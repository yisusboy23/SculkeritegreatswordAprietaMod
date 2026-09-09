package net.tucas.sculkeritegreatsword.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.item.Moditems;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sculkeritegreatsword.MOD_ID);


    public static final RegistryObject<CreativeModeTab> SCULKERITE_TAB =
            TABS.register("sculkerite_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sculkerite_tab"))
                    .icon(() -> new ItemStack(Moditems.SCULKERITEGREATSWORD.get()))
                    .displayItems((parameters, output) -> {
                        // Items
                        output.accept(Moditems.SCULKERITEGREATSWORD.get());
                        output.accept(Moditems.SCULKERITE_SCYTHE.get());
                        output.accept(Moditems.GOODBYE_TO_A_WORLD_MUSIC_DISC.get());
                        output.accept(Moditems.PAXEL.get());
                        output.accept(Moditems.DRAGON_PAXEL.get());
                        output.accept(Moditems.TERSECTACT.get());
                        output.accept(Moditems.MANDRAKE_ROOT.get());
                        output.accept(Moditems.GOLDEN_MANDRAKE.get());
                        // Bloques
                        output.accept(Moditems.GEAR_COPPER_BLOCK.get());
                        output.accept(Moditems.KRILLATHAN_EGG.get());
                        output.accept(Moditems.BULLSQUAMA_EGG.get());
                        output.accept(Moditems.DRILLER_EGG.get());
                        output.accept(Moditems.MUDDER_EGG.get());
                        output.accept(Moditems.WOOLODON_EGG.get());
                        output.accept(Moditems.BABY_KRILLATHAN_BUCKET.get());
                        output.accept(Moditems.MUDDER_BUCKET.get());
                        output.accept(Moditems.KELP_ON_A_STICK.get());
                        output.accept(Moditems.DRILLER_CLAW.get());
                        output.accept(Moditems.PEEKER_FETUS.get());
                        output.accept(Moditems.SPORES_SHROOMERS.get());
                        // Spawn Eggs
                        output.accept(Moditems.MUSHROOM_SERVANT_SPAWN_EGG.get());
                        output.accept(Moditems.SCULK_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.OXICOPPER_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.MUSHROOM_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.LAPIS_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.GRINDSTONE_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.DIAMOND_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.CHORUS_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.HYDRANTOR_GOLEM_SPAWN_EGG.get());
                        output.accept(Moditems.DRILLER_SPAWN_EGG.get());
                        output.accept(Moditems.WOOLODON_SPAWN_EGG.get());
                        output.accept(Moditems.BULLSQUAMA_SPAWN_EGG.get());
                        output.accept(Moditems.MUDDER_SPAWN_EGG.get());
                        output.accept(Moditems.KRILLATHAN_SPAWN_EGG.get());
                        output.accept(Moditems.PEEKER_SPAWN_EGG.get());
                        output.accept(Moditems.MANDRAKE_SPAWN_EGG.get());
                        output.accept(Moditems.FORGOTTEN_DRONE_SPAWN_EGG.get());
                        output.accept(Moditems.FORGOTTEN_TOY_SPAWN_EGG.get());
                        output.accept(Moditems.RESONARCH_SPAWN_EGG.get());

                        // Libro 1 - Fast Recharge
                        ItemStack fastRechargeBook = new ItemStack(Items.ENCHANTED_BOOK);
                        ListTag enchantments1 = new ListTag();
                        CompoundTag enchantment1 = new CompoundTag();
                        enchantment1.putString("id", BuiltInRegistries.ENCHANTMENT.getKey(ModEnchantments.FASTRECHARGE.get()).toString());
                        enchantment1.putShort("lvl", (short) 1);
                        enchantments1.add(enchantment1);
                        fastRechargeBook.getOrCreateTag().put("StoredEnchantments", enchantments1);
                        output.accept(fastRechargeBook);

                        // Libro 2 - Kinetic Energy
                        ItemStack kineticEnergyBook = new ItemStack(Items.ENCHANTED_BOOK);
                        ListTag enchantments2 = new ListTag();
                        CompoundTag enchantment2 = new CompoundTag();
                        enchantment2.putString("id", BuiltInRegistries.ENCHANTMENT.getKey(ModEnchantments.KINETICENERGY.get()).toString());
                        enchantment2.putShort("lvl", (short) 1);
                        enchantments2.add(enchantment2);
                        kineticEnergyBook.getOrCreateTag().put("StoredEnchantments", enchantments2);
                        output.accept(kineticEnergyBook);
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.minecraft.world.item.BlockItem;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.minecraft.world.item.Tiers;

public class Moditems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<Item> PAXEL = ITEMS.register("paxel",
            () -> new PaxelItem(Tiers.DIAMOND, 4, -2.8F,
                    new Item.Properties().durability(1561)));

    public static final RegistryObject<Item> DRAGON_PAXEL = ITEMS.register("dragon_paxel",
            () -> new PaxelItem(Tiers.NETHERITE, 7, -2.8F,
                    new Item.Properties().durability(4062).fireResistant()));

    public static final RegistryObject<Item> DRILLER_CLAW = ITEMS.register("driller_claw",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SCULKERITEGREATSWORD =
            ITEMS.register("sculkeritegreatsword",
                    SculkeritegreatswordItem::new);

    public static final RegistryObject<Item> SCULKERITE_SCYTHE =
            ITEMS.register("sculkerite_scythe",
                    SculkeriteScytheItem::new);

    public static final RegistryObject<Item> GOODBYE_TO_A_WORLD_MUSIC_DISC =
            ITEMS.register("goodbye_to_a_world_music_disc",
                    GoodbyeToAWorldDiscItem::new);

    public static final RegistryObject<Item> TERSECTACT = ITEMS.register("tersectact",
            () -> new TersectactItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> KRILLATHAN_EGG =
            ITEMS.register("krillathan_eggs",
                    () -> new BlockItem(ModBlocks.KRILLATHAN_EGG.get(), new Item.Properties()));

    public static final RegistryObject<Item> BABY_KRILLATHAN_BUCKET =
            ITEMS.register("baby_krillathan_bucket",
                    () -> new BabyKrillathanBucketItem(
                            new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> KELP_ON_A_STICK = ITEMS.register("kelp_on_a_stick",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRILLER_EGG = ITEMS.register("driller_egg",
            () -> new BlockItem(ModBlocks.DRILLER_EGG.get(), new Item.Properties()));
    public static final RegistryObject<Item> BULLSQUAMA_EGG =
            ITEMS.register("bullsquama_egg",
                    () -> new BlockItem(ModBlocks.BULLSQUAMA_EGG.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> SCULK_GOLEM_SPAWN_EGG =
            ITEMS.register("sculk_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.SCULK_GOLEM,
                            0x2C2C2C,
                            0x0F5E5A,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> OXICOPPER_GOLEM_SPAWN_EGG =
            ITEMS.register("oxicopper_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.OXICOPPER_GOLEM,
                            0xC77340,
                            0x4A7C59,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> MUSHROOM_GOLEM_SPAWN_EGG =
            ITEMS.register("mushroom_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.MUSHROOM_GOLEM,
                            0xDC143C,
                            0xF5F5F5,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> LAPIS_GOLEM_SPAWN_EGG =
            ITEMS.register("lapis_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.LAPIS_GOLEM,
                            0x1E40AF,
                            0x60A5FA,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> GRINDSTONE_GOLEM_SPAWN_EGG =
            ITEMS.register("grindstone_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.GRINDSTONE_GOLEM,
                            0x7A7A7A,
                            0x8B5A2B,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> DIAMOND_GOLEM_SPAWN_EGG =
            ITEMS.register("diamond_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.DIAMOND_GOLEM,
                            0x5DCCDB,
                            0xB9F2FF,
                            new Item.Properties().rarity(Rarity.RARE)
                    ));

    public static final RegistryObject<Item> CHORUS_GOLEM_SPAWN_EGG =
            ITEMS.register("chorus_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.CHORUS_GOLEM,
                            0xE8D4F7,
                            0x4A1E6B,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> HYDRANTOR_GOLEM_SPAWN_EGG =
            ITEMS.register("hydrantor_golem_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.HYDRANTOR_GOLEM,
                            0xC46200,
                            0x4682B4,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> DRILLER_SPAWN_EGG =
            ITEMS.register("driller_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.DRILLER,
                            0x8B4513,
                            0x1E40AF,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    // ── Krillathan Spawn Egg  (azul principal, morado azulado secundario) ──
    public static final RegistryObject<Item> KRILLATHAN_SPAWN_EGG =
            ITEMS.register("krillathan_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.KRILLATHAN,
                            0x1A6ECC,  // Azul oceánico principal
                            0x5B3A8A,  // Morado azulado secundario
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));
    public static final RegistryObject<Item> BULLSQUAMA_SPAWN_EGG =
            ITEMS.register("bullsquama_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.BULLSQUAMA,
                            0x90EE90,  // verde claro
                            0xFFCC99,  // naranja claro
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));
    public static final RegistryObject<Item> MUDDER_SPAWN_EGG =
            ITEMS.register("mudder_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.MUDDER,
                            0xFFA500,  // Amarillo naranjoso (naranja)
                            0x800080,  // Morado
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));
    public static final RegistryObject<Item> PEEKER_SPAWN_EGG =
            ITEMS.register("peeker_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.PEEKER,
                            0x1A1A1A,  // Negro principal
                            0x808080,  // Gris secundario
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));
    public static final RegistryObject<Item> MUDDER_BUCKET =
            ITEMS.register("mudder_bucket",
                    () -> new MudderBucketItem(
                            new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MUDDER_EGG =
            ITEMS.register("mudder_egg",
                    () -> new BlockItem(ModBlocks.MUDDER_EGG.get(), new Item.Properties()));
    public static final RegistryObject<Item> PEEKER_FETUS =
            ITEMS.register("peeker_fetus",
                    () -> new BlockItem(ModBlocks.PEEKER_FETUS.get(), new Item.Properties()));
    public static final RegistryObject<Item> MANDRAKE_ROOT = ITEMS.register("mandrake_root",
            () -> new MandrakeRoot(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANDRAKE_ROOT_PLANT = ITEMS.register("mandrake_root_plant",
            () -> new BlockItem(ModBlocks.MANDRAKE_ROOT_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> GEAR_COPPER_BLOCK = ITEMS.register("gear_copper_block",
            () -> new BlockItem(ModBlocks.GEAR_COPPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> MUSHROOM_SERVANT_SPAWN_EGG =
            ITEMS.register("mushroom_servant_spawn_egg",
                    () -> new MushroomServantSpawnEggItem(
                            new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANDRAKE_SPAWN_EGG =
            ITEMS.register("mandrake_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.MANDRAKE,
                            0x4A7C3A,  // Verde raíz principal
                            0x8B5A2B,  // Marrón tierra secundario
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
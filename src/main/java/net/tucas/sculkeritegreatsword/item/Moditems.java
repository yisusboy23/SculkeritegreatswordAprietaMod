package net.tucas.sculkeritegreatsword.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.init.ModBlocks;
import net.tucas.sculkeritegreatsword.init.ModEntities;

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

    public static final RegistryObject<Item> SPORES_SHROOMERS = ITEMS.register("spores_shroomers",
            () -> new SporesShroomersItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DRILLER_EGG = ITEMS.register("driller_egg",
            () -> new BlockItem(ModBlocks.DRILLER_EGG.get(), new Item.Properties()));

    public static final RegistryObject<Item> BULLSQUAMA_EGG =
            ITEMS.register("bullsquama_egg",
                    () -> new BlockItem(ModBlocks.BULLSQUAMA_EGG.get(),
                            new Item.Properties()));

    public static final RegistryObject<Item> WOOLODON_EGG =
            ITEMS.register("brachiosaurus_egg",
                    () -> new BlockItem(ModBlocks.WOOLODON_EGG.get(), new Item.Properties()));

    // MANDRÁGORAS
    public static final RegistryObject<Item> MANDRAKE_ROOT = ITEMS.register("mandrake_root",
            () -> new MandrakeRoot(ModBlocks.MANDRAKE_ROOT_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GOLDEN_MANDRAKE = ITEMS.register("golden_mandrake",
            () -> new GoldenMandrakeItem(new Item.Properties().rarity(Rarity.RARE)));

    // SPAWN EGGS
    public static final RegistryObject<Item> SCULK_GOLEM_SPAWN_EGG =
            ITEMS.register("sculk_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.SCULK_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> OXICOPPER_GOLEM_SPAWN_EGG =
            ITEMS.register("oxicopper_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.OXICOPPER_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> MUSHROOM_GOLEM_SPAWN_EGG =
            ITEMS.register("mushroom_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.MUSHROOM_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> LAPIS_GOLEM_SPAWN_EGG =
            ITEMS.register("lapis_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.LAPIS_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> GRINDSTONE_GOLEM_SPAWN_EGG =
            ITEMS.register("grindstone_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.GRINDSTONE_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> DIAMOND_GOLEM_SPAWN_EGG =
            ITEMS.register("diamond_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.DIAMOND_GOLEM,
                            new Item.Properties().rarity(Rarity.RARE)
                    ));

    public static final RegistryObject<Item> CHORUS_GOLEM_SPAWN_EGG =
            ITEMS.register("chorus_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.CHORUS_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> HYDRANTOR_GOLEM_SPAWN_EGG =
            ITEMS.register("hydrantor_golem_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.HYDRANTOR_GOLEM,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> DRILLER_SPAWN_EGG =
            ITEMS.register("driller_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.DRILLER,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> KRILLATHAN_SPAWN_EGG =
            ITEMS.register("krillathan_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.KRILLATHAN,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> BULLSQUAMA_SPAWN_EGG =
            ITEMS.register("bullsquama_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.BULLSQUAMA,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> MUDDER_SPAWN_EGG =
            ITEMS.register("mudder_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.MUDDER,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> PEEKER_SPAWN_EGG =
            ITEMS.register("peeker_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.PEEKER,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> MUSHROOM_SERVANT_SPAWN_EGG =
            ITEMS.register("mushroom_servant_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.MUSHROOM_SERVANT,
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    ));

    public static final RegistryObject<Item> MANDRAKE_SPAWN_EGG =
            ITEMS.register("mandrake_spawn_egg",
                    () -> new CustomSpawnEggItem(
                            ModEntities.MANDRAKE,
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

    public static final RegistryObject<Item> GEAR_COPPER_BLOCK = ITEMS.register("gear_copper_block",
            () -> new BlockItem(ModBlocks.GEAR_COPPER_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
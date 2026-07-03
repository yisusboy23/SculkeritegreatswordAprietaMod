package net.tucas.sculkeritegreatsword.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.custom.DrillerEntity;
import net.tucas.sculkeritegreatsword.entity.SculkboomparticleEntity;
import net.tucas.sculkeritegreatsword.entity.custom.SculkGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.OxicopperGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;
import net.tucas.sculkeritegreatsword.entity.custom.LapisGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.GrindstoneGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.DiamondGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.ChorusGolemEntity;
import net.tucas.sculkeritegreatsword.entity.custom.HydrantorGolemEntity;
import net.tucas.sculkeritegreatsword.entity.projectile.DiamondFrostProjectileEntity;
import net.tucas.sculkeritegreatsword.entity.custom.KrillathanEntity;
import net.tucas.sculkeritegreatsword.entity.custom.BullsquamaEntity;
import net.tucas.sculkeritegreatsword.entity.custom.MudderEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sculkeritegreatsword.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Sculkeritegreatsword.MOD_ID);

    public static final RegistryObject<EntityType<SculkboomparticleEntity>> SCULKBOOMPARTICLE = register(
            "sculkboomparticle",
            EntityType.Builder.<SculkboomparticleEntity>of(SculkboomparticleEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .sized(0.5f, 0.5f)
                    .setCustomClientFactory(SculkboomparticleEntity::new)
    );

    public static final RegistryObject<EntityType<SculkGolemEntity>> SCULK_GOLEM = ENTITIES.register(
            "sculk_golem",
            () -> EntityType.Builder
                    .<SculkGolemEntity>of(SculkGolemEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.6f)
                    .build("sculk_golem")
    );
    public static final RegistryObject<EntityType<DrillerEntity>> DRILLER = ENTITIES.register(
            "driller",
            () -> EntityType.Builder
                    .<DrillerEntity>of(DrillerEntity::new, MobCategory.CREATURE)
                    .sized(1.6f, 2.2f)
                    .build("driller")
    );

    public static final RegistryObject<EntityType<OxicopperGolemEntity>> OXICOPPER_GOLEM = ENTITIES.register(
            "oxicopper_golem",
            () -> EntityType.Builder
                    .<OxicopperGolemEntity>of(OxicopperGolemEntity::new, MobCategory.CREATURE)
                    .sized(0.8f, 2.4f)
                    .build("oxicopper_golem")
    );

    public static final RegistryObject<EntityType<MushroomGolemEntity>> MUSHROOM_GOLEM = ENTITIES.register(
            "mushroom_golem",
            () -> EntityType.Builder
                    .<MushroomGolemEntity>of(MushroomGolemEntity::new, MobCategory.CREATURE)
                    .sized(0.9f, 2.0f)
                    .build("mushroom_golem")
    );

    public static final RegistryObject<EntityType<MushroomServantEntity>> MUSHROOM_SERVANT = ENTITIES.register(
            "mushroom_servant",
            () -> EntityType.Builder
                    .<MushroomServantEntity>of(MushroomServantEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.8f)
                    .build("mushroom_servant")
    );

    public static final RegistryObject<EntityType<LapisGolemEntity>> LAPIS_GOLEM = ENTITIES.register(
            "lapis_golem",
            () -> EntityType.Builder
                    .<LapisGolemEntity>of(LapisGolemEntity::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.0f)
                    .build("lapis_golem")
    );
    public static final RegistryObject<EntityType<GrindstoneGolemEntity>> GRINDSTONE_GOLEM = ENTITIES.register(
            "grindstone_golem",
            () -> EntityType.Builder
                    .<GrindstoneGolemEntity>of(GrindstoneGolemEntity::new, MobCategory.CREATURE)
                    .sized(1.2f, 1.0f) // Ajusta el tamaño según tu modelo
                    .build("grindstone_golem")
    );
    // Agregar después de GRINDSTONE_GOLEM:
    public static final RegistryObject<EntityType<DiamondGolemEntity>> DIAMOND_GOLEM = ENTITIES.register(
            "diamond_golem",
            () -> EntityType.Builder
                    .<DiamondGolemEntity>of(DiamondGolemEntity::new, MobCategory.CREATURE)
                    .sized(1.4f, 2.2f)
                    .build("diamond_golem")
    );
    public static final RegistryObject<EntityType<DiamondFrostProjectileEntity>> DIAMOND_FROST_PROJECTILE = register(
            "diamond_frost_projectile",
            EntityType.Builder.<DiamondFrostProjectileEntity>of(DiamondFrostProjectileEntity::new, MobCategory.MISC)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .sized(0.8f, 0.5f)
    );


    public static final RegistryObject<EntityType<ChorusGolemEntity>> CHORUS_GOLEM = ENTITIES.register(
            "chorus_golem",
            () -> EntityType.Builder
                    .<ChorusGolemEntity>of(ChorusGolemEntity::new, MobCategory.CREATURE)
                    .sized(2.4f, 3.0f)
                    .build("chorus_golem")
    );

    public static final RegistryObject<EntityType<HydrantorGolemEntity>> HYDRANTOR_GOLEM = ENTITIES.register(
            "hydrantor_golem",
            () -> EntityType.Builder
                    .<HydrantorGolemEntity>of(HydrantorGolemEntity::new, MobCategory.CREATURE)
                    .sized(2.0f, 2.8f) // Ajusta el tamaño según tu modelo
                    .build("hydrantor_golem")
    );

    public static final RegistryObject<EntityType<KrillathanEntity>> KRILLATHAN = ENTITIES.register(
            "krillathan",
            () -> EntityType.Builder
                    .<KrillathanEntity>of(KrillathanEntity::new, MobCategory.WATER_CREATURE)
                    .sized(4.0f, 4.0f)  // ← CAMBIADO A SERPIENTE HORIZONTAL
                    .build("krillathan")
    );
    public static final RegistryObject<EntityType<KrillathanEntity>> KRILLATHAN_BABY = ENTITIES.register(
            "krillathan_baby",
            () -> EntityType.Builder
                    .<KrillathanEntity>of(KrillathanEntity::new, MobCategory.WATER_CREATURE)
                    .sized(1.6f, 1.0f)  // hitbox pequeña para el bebé
                    .build("krillathan_baby")
    );
    public static final RegistryObject<EntityType<BullsquamaEntity>> BULLSQUAMA = ENTITIES.register(
            "bullsquama",
            () -> EntityType.Builder
                    .<BullsquamaEntity>of(BullsquamaEntity::new, MobCategory.CREATURE)
                    .sized(2.6f, 2.3f)
                    .build("bullsquama")
    );

    // ── MUDDER ────────────────────────────────────────────────────────────────
    public static final RegistryObject<EntityType<MudderEntity>> MUDDER = ENTITIES.register(
            "mudder",
            () -> EntityType.Builder
                    .<MudderEntity>of(MudderEntity::new, MobCategory.CREATURE)
                    .sized(0.9f, 0.5f)   // ajusta al tamaño real de tu modelo
                    .build("mudder")
    );
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(SCULK_GOLEM.get(), SculkGolemEntity.createAttributes().build());
        event.put(SCULKBOOMPARTICLE.get(), SculkboomparticleEntity.createAttributes().build());
        event.put(OXICOPPER_GOLEM.get(), OxicopperGolemEntity.createAttributes().build());
        event.put(MUSHROOM_GOLEM.get(), MushroomGolemEntity.createAttributes().build());
        event.put(MUSHROOM_SERVANT.get(), MushroomServantEntity.createAttributes().build());
        event.put(LAPIS_GOLEM.get(), LapisGolemEntity.createAttributes().build());
        event.put(GRINDSTONE_GOLEM.get(), GrindstoneGolemEntity.createAttributes().build());
        event.put(DIAMOND_GOLEM.get(), DiamondGolemEntity.createAttributes().build());
        event.put(CHORUS_GOLEM.get(), ChorusGolemEntity.createAttributes().build());
        event.put(HYDRANTOR_GOLEM.get(), HydrantorGolemEntity.createAttributes().build());
        event.put(DRILLER.get(), DrillerEntity.createAttributes().build());
        event.put(KRILLATHAN.get(), KrillathanEntity.createAttributes().build());
        event.put(KRILLATHAN_BABY.get(), KrillathanEntity.createAttributes().build());
        event.put(BULLSQUAMA.get(), BullsquamaEntity.createAttributes().build());
        event.put(MUDDER.get(), MudderEntity.createAttributes().build());

    }
    private static <T extends net.minecraft.world.entity.Entity> RegistryObject<EntityType<T>> register(
            String registryname,
            EntityType.Builder<T> builder
    ) {
        return ENTITIES.register(registryname, () -> builder.build(registryname));
    }

    public static void register(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
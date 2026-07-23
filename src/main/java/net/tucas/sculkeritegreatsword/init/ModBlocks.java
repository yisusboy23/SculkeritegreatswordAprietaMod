package net.tucas.sculkeritegreatsword.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.block.KrillathanEggBlock;
import net.tucas.sculkeritegreatsword.block.DrillerEggBlock;
import net.tucas.sculkeritegreatsword.block.BullsquamaEggBlock;
import net.tucas.sculkeritegreatsword.block.MudderEggBlock;
import net.tucas.sculkeritegreatsword.block.MandrakeRootBlock;
import net.minecraft.world.level.block.SoundType;
import net.tucas.sculkeritegreatsword.block.PeekerFetusBlock;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Sculkeritegreatsword.MOD_ID);

    // Bloque de engranaje de cobre con durabilidad del cobre
    public static final RegistryObject<Block> GEAR_COPPER_BLOCK = BLOCKS.register("gear_copper_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3.0f) // Durabilidad del cobre
                    .requiresCorrectToolForDrops() // Requiere pico
                    .sound(SoundType.METAL))); // Sonido de metal
    public static final RegistryObject<Block> KRILLATHAN_EGG = BLOCKS.register("krillathan_eggs",
            () -> new KrillathanEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .sound(SoundType.CORAL_BLOCK)
                    .noOcclusion()));
    public static final RegistryObject<Block> DRILLER_EGG = BLOCKS.register("driller_egg",
            () -> new DrillerEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .randomTicks()
            )
    );

    public static final RegistryObject<Block> BULLSQUAMA_EGG =
            BLOCKS.register("bullsquama_egg",
                    () -> new BullsquamaEggBlock(BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.BONE_BLOCK)
                            .noOcclusion()));

    public static final RegistryObject<Block> MUDDER_EGG =
            BLOCKS.register("mudder_egg",
                    () -> new MudderEggBlock(BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.BONE_BLOCK)
                            .noOcclusion()));

    public static final RegistryObject<Block> PEEKER_FETUS =
            BLOCKS.register("peeker_fetus",
                    () -> new PeekerFetusBlock(BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.BONE_BLOCK)
                            .noOcclusion()
                            .randomTicks()));
    public static final RegistryObject<Block> MANDRAKE_ROOT_BLOCK =
            BLOCKS.register("mandrake_root_block",
                    () -> new MandrakeRootBlock(BlockBehaviour.Properties.of()
                            .strength(0.0F)
                            .sound(SoundType.CROP)
                            .noCollission()
                            .instabreak()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
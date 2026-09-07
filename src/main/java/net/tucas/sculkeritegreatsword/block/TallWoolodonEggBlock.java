package net.tucas.sculkeritegreatsword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TallWoolodonEggBlock extends BullsquamaEggBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF =
            BlockStateProperties.DOUBLE_BLOCK_HALF;

    private final VoxelShape upperShape;

    /*
     * ============================================================
     * TIEMPO DE PRUEBA
     * ============================================================
     *
     * 20 ticks = 1 segundo
     * 40 ticks = 2 segundos
     *
     * 1 segundo -> primera grieta
     * 2 segundos -> segunda grieta
     * 2 segundos -> eclosión
     *
     * TOTAL = 5 SEGUNDOS
     */

    private static final int INITIAL_DELAY_TICKS = 6000;
    private static final int STAGE_DELAY_TICKS = 6000;

    private static final int MAX_HATCH_STAGE = 2;

    public TallWoolodonEggBlock(Properties properties) {
        super(properties);

        /*
         * ========================================================
         * PARTE SUPERIOR
         * ========================================================
         *
         * 16 x 16 x 8 píxeles.
         */
        this.upperShape = Block.box(
                0,
                0,
                0,
                16,
                8,
                16
        );

        /*
         * Estado inicial del huevo.
         *
         * LOWER
         * HATCH 0
         */
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(
                                HALF,
                                DoubleBlockHalf.LOWER
                        )
                        .setValue(
                                HATCH,
                                0
                        )
        );
    }

    /*
     * ============================================================
     * COLOCACIÓN
     * ============================================================
     *
     * Comprueba que exista espacio para las dos partes.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        /*
         * Necesitamos espacio encima.
         */
        if (pos.getY() >= level.getMaxBuildHeight() - 1) {
            return null;
        }

        BlockState upperState =
                level.getBlockState(pos.above());

        if (!upperState.canBeReplaced(context)) {
            return null;
        }

        /*
         * La parte que coloca el jugador siempre es LOWER.
         */
        return this.defaultBlockState()
                .setValue(
                        HALF,
                        DoubleBlockHalf.LOWER
                )
                .setValue(
                        HATCH,
                        0
                );
    }

    /*
     * ============================================================
     * COLOCAR LA SEGUNDA MITAD
     * ============================================================
     *
     * Primero creamos UPPER.
     * Después iniciamos el contador.
     */
    @Override
    public void setPlacedBy(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            LivingEntity placer,
            @NotNull ItemStack stack
    ) {
        /*
         * El padre no tiene una lógica especial que
         * necesitemos aquí, pero mantenemos la llamada
         * por compatibilidad.
         */
        super.setPlacedBy(
                level,
                pos,
                state,
                placer,
                stack
        );

        BlockPos upperPos = pos.above();

        /*
         * Crear la parte superior.
         */
        BlockState upperState =
                this.defaultBlockState()
                        .setValue(
                                HALF,
                                DoubleBlockHalf.UPPER
                        )
                        .setValue(
                                HATCH,
                                state.getValue(HATCH)
                        );

        upperState = copyWaterloggedFrom(
                level,
                upperPos,
                upperState
        );

        level.setBlock(
                upperPos,
                upperState,
                3
        );

        /*
         * ========================================================
         * INICIAR CONTADOR
         * ========================================================
         *
         * IMPORTANTE:
         * Solo LOWER programa el tick.
         *
         * La primera grieta aparecerá después de 20 ticks.
         */
        if (!level.isClientSide) {

            level.scheduleTick(
                    pos,
                    this,
                    INITIAL_DELAY_TICKS
            );
        }
    }

    /*
     * ============================================================
     * ON PLACE
     * ============================================================
     *
     * NO llamamos a super.onPlace().
     *
     * Esto es MUY importante porque el BullsquamaEggBlock
     * padre tiene:
     *
     * scheduleTick(..., 12000)
     *
     * y no queremos ese temporizador para Woolodon.
     */
    @Override
    public void onPlace(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState oldState,
            boolean moving
    ) {
        /*
         * Intencionalmente vacío.
         *
         * El temporizador se inicia en setPlacedBy().
         */
    }

    /*
     * ============================================================
     * SUPERVIVENCIA
     * ============================================================
     */
    @Override
    public boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        DoubleBlockHalf half =
                state.getValue(HALF);

        /*
         * UPPER necesita LOWER debajo.
         */
        if (half == DoubleBlockHalf.UPPER) {

            BlockState below =
                    level.getBlockState(pos.below());

            return below.is(this)
                    && below.getValue(HALF)
                    == DoubleBlockHalf.LOWER;
        }

        /*
         * LOWER utiliza la misma lógica de supervivencia
         * del BullsquamaEggBlock.
         */
        return super.canSurvive(
                state,
                level,
                pos
        );
    }

    /*
     * ============================================================
     * WATERLOGGED
     * ============================================================
     */
    public static BlockState copyWaterloggedFrom(
            LevelReader level,
            BlockPos pos,
            BlockState state
    ) {
        if (state.hasProperty(
                BlockStateProperties.WATERLOGGED
        )) {

            return state.setValue(
                    BlockStateProperties.WATERLOGGED,
                    level.getFluidState(pos).getType()
                            == net.minecraft.world.level.material.Fluids.WATER
            );
        }

        return state;
    }

    /*
     * ============================================================
     * TICK DE ECLOSIÓN
     * ============================================================
     *
     * LOWER controla todo.
     *
     * HATCH 0 -> 1
     * HATCH 1 -> 2
     * HATCH 2 -> Woolodon
     */
    @Override
    public void tick(
            @NotNull BlockState state,
            @NotNull ServerLevel level,
            @NotNull BlockPos pos,
            @NotNull RandomSource random
    ) {
        /*
         * UPPER nunca controla los ticks.
         */
        if (state.getValue(HALF)
                != DoubleBlockHalf.LOWER) {

            return;
        }

        int hatch =
                state.getValue(HATCH);

        /*
         * ========================================================
         * HATCH 0 -> 1
         * HATCH 1 -> 2
         * ========================================================
         */
        if (hatch < MAX_HATCH_STAGE) {

            int newHatch =
                    hatch + 1;

            /*
             * Sonido de grieta.
             */
            level.playSound(
                    null,
                    pos,
                    SoundEvents.SNIFFER_EGG_CRACK,
                    SoundSource.BLOCKS,
                    0.7F,
                    0.9F + random.nextFloat() * 0.2F
            );

            /*
             * Actualizar LOWER.
             */
            level.setBlock(
                    pos,
                    state.setValue(
                            HATCH,
                            newHatch
                    ),
                    2
            );

            /*
             * Actualizar UPPER.
             */
            BlockPos upperPos =
                    pos.above();

            BlockState upperState =
                    level.getBlockState(
                            upperPos
                    );

            if (upperState.is(this)
                    && upperState.getValue(HALF)
                    == DoubleBlockHalf.UPPER) {

                level.setBlock(
                        upperPos,
                        upperState.setValue(
                                HATCH,
                                newHatch
                        ),
                        2
                );
            }

            /*
             * Esperar 2 segundos.
             */
            level.scheduleTick(
                    pos,
                    this,
                    STAGE_DELAY_TICKS
            );

            return;
        }

        /*
         * ========================================================
         * HATCH 2 -> ECLOSIÓN
         * ========================================================
         */
        hatch(
                level,
                pos,
                random
        );
    }

    /*
     * ============================================================
     * ECLOSIÓN
     * ============================================================
     */
    private void hatch(
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        /*
         * Sonido de eclosión.
         */
        level.playSound(
                null,
                pos,
                SoundEvents.SNIFFER_EGG_HATCH,
                SoundSource.BLOCKS,
                0.7F,
                0.9F + random.nextFloat() * 0.2F
        );

        /*
         * ========================================================
         * ELIMINAR UPPER
         * ========================================================
         */
        BlockPos upperPos =
                pos.above();

        BlockState upperState =
                level.getBlockState(
                        upperPos
                );

        if (upperState.is(this)
                && upperState.getValue(HALF)
                == DoubleBlockHalf.UPPER) {

            level.setBlock(
                    upperPos,
                    Blocks.AIR.defaultBlockState(),
                    35
            );
        }

        /*
         * ========================================================
         * ELIMINAR LOWER
         * ========================================================
         */
        level.setBlock(
                pos,
                Blocks.AIR.defaultBlockState(),
                35
        );

        /*
         * ========================================================
         * CREAR WOOLODON
         * ========================================================
         */
        Entity entity =
                ModEntities.WOOLODON
                        .get()
                        .create(level);

        /*
         * Si el registro de la entidad devuelve null,
         * no intentamos continuar.
         */
        if (entity == null) {
            return;
        }

        if (entity instanceof Mob mob) {

            /*
             * ====================================================
             * HACERLO BEBÉ
             * ====================================================
             */
            if (entity instanceof Animal animal) {
                animal.setBaby(true);
            }

            /*
             * ====================================================
             * POSICIÓN
             * ====================================================
             */
            entity.moveTo(
                    pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    random.nextFloat() * 360F,
                    0F
            );

            /*
             * ====================================================
             * FINALIZAR SPAWN
             * ====================================================
             */
            ForgeEventFactory.onFinalizeSpawn(
                    mob,
                    level,
                    level.getCurrentDifficultyAt(pos),
                    MobSpawnType.NATURAL,
                    null,
                    null
            );

            /*
             * ====================================================
             * AÑADIR AL MUNDO
             * ====================================================
             */
            level.addFreshEntity(
                    entity
            );
        }
    }

    /*
     * ============================================================
     * DESTRUIR EL HUEVO
     * ============================================================
     */
    @Override
    public void playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        if (!level.isClientSide) {

            /*
             * Si se rompe UPPER en creativo,
             * eliminar también LOWER.
             */
            if (player.isCreative()
                    && state.getValue(HALF)
                    == DoubleBlockHalf.UPPER) {

                BlockPos below =
                        pos.below();

                BlockState belowState =
                        level.getBlockState(
                                below
                        );

                if (belowState.is(this)
                        && belowState.getValue(HALF)
                        == DoubleBlockHalf.LOWER) {

                    level.setBlock(
                            below,
                            Blocks.AIR.defaultBlockState(),
                            35
                    );

                    level.levelEvent(
                            player,
                            2001,
                            below,
                            Block.getId(
                                    belowState
                            )
                    );
                }
            }
        }

        super.playerWillDestroy(
                level,
                pos,
                state,
                player
        );
    }

    /*
     * ============================================================
     * FORMA
     * ============================================================
     */
    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter getter,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        /*
         * LOWER usa la forma del BullsquamaEggBlock.
         */
        if (state.getValue(HALF)
                == DoubleBlockHalf.LOWER) {

            return super.getShape(
                    state,
                    getter,
                    pos,
                    context
            );
        }

        /*
         * UPPER usa su propia forma.
         */
        return upperShape;
    }

    /*
     * ============================================================
     * UPDATE SHAPE
     * ============================================================
     */
    @Override
    public @NotNull BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos
    ) {
        DoubleBlockHalf half =
                state.getValue(HALF);

        /*
         * ========================================================
         * UPPER
         * ========================================================
         *
         * Si ya no existe LOWER debajo,
         * eliminar UPPER.
         */
        if (half == DoubleBlockHalf.UPPER
                && direction == Direction.DOWN) {

            if (!neighbourState.is(this)
                    || neighbourState.getValue(HALF)
                    != DoubleBlockHalf.LOWER) {

                return Blocks.AIR.defaultBlockState();
            }
        }

        /*
         * ========================================================
         * LOWER
         * ========================================================
         *
         * Si ya no existe UPPER encima,
         * eliminar LOWER.
         */
        if (half == DoubleBlockHalf.LOWER
                && direction == Direction.UP) {

            if (!neighbourState.is(this)
                    || neighbourState.getValue(HALF)
                    != DoubleBlockHalf.UPPER) {

                return Blocks.AIR.defaultBlockState();
            }
        }

        /*
         * ========================================================
         * SOPORTE
         * ========================================================
         */
        if (half == DoubleBlockHalf.LOWER
                && direction == Direction.DOWN
                && !state.canSurvive(level, pos)) {

            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(
                state,
                direction,
                neighbourState,
                level,
                pos,
                neighbourPos
        );
    }

    /*
     * ============================================================
     * BLOCK STATES
     * ============================================================
     */
    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(
                builder
        );

        builder.add(HALF);
    }
}
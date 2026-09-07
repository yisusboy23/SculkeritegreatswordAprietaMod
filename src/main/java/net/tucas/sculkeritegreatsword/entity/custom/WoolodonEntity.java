package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.tucas.sculkeritegreatsword.entity.ai.goal.WoolodonEatGrassGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WoolodonEntity extends Animal implements GeoEntity {
    // =========================================================
    // ANIMACIONES
    // =========================================================

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("walk");

    private static final RawAnimation RUN =
            RawAnimation.begin().thenLoop("run");

    private static final RawAnimation EAT =
            RawAnimation.begin().thenPlay("eat");


    // =========================================================
    // GECKOLIB
    // =========================================================

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);


    // =========================================================
    // COMIDA
    // =========================================================

    private int eatCooldown = 0;

    private static final int EAT_COOLDOWN = 100;


    // =========================================================
    // HUIDA
    // =========================================================

    private int fleeTicks = 0;

    private static final int FLEE_DURATION = 60;

    private static final EntityDataAccessor<Boolean> FLEEING =
            SynchedEntityData.defineId(
                    WoolodonEntity.class,
                    EntityDataSerializers.BOOLEAN
            );


    // =========================================================
    // ESTADO DE LA LANA
    //
    // Cada bit representa una parte de lana:
    //
    // 0 = lana
    // 1 = lana2
    // 2 = lana3
    // 3 = lana4
    // 4 = lana5
    //
    // 0  = toda la lana está presente
    // 31 = todas las partes están cortadas
    // =========================================================

    private static final EntityDataAccessor<Integer> WOOL_STATE =
            SynchedEntityData.defineId(
                    WoolodonEntity.class,
                    EntityDataSerializers.INT
            );

    public static final int WOOL_LANA = 1 << 0;
    public static final int WOOL_LANA2 = 1 << 1;
    public static final int WOOL_LANA3 = 1 << 2;
    public static final int WOOL_LANA4 = 1 << 3;
    public static final int WOOL_LANA5 = 1 << 4;

    private static final int ALL_WOOL_CUT =
            WOOL_LANA |
                    WOOL_LANA2 |
                    WOOL_LANA3 |
                    WOOL_LANA4 |
                    WOOL_LANA5;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public WoolodonEntity(
            EntityType<? extends Animal> type,
            Level level
    ) {
        super(type, level);
    }


    // =========================================================
    // DATOS SINCRONIZADOS
    // =========================================================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(
                FLEEING,
                false
        );

        // 0 = toda la lana presente
        this.entityData.define(
                WOOL_STATE,
                0
        );
    }


    // =========================================================
    // COMIDA
    // =========================================================

    public int getEatCooldown() {
        return eatCooldown;
    }

    public void setEatCooldown(int eatCooldown) {
        this.eatCooldown = eatCooldown;
    }


    // =========================================================
    // HUIDA
    // =========================================================

    public boolean isFleeing() {
        return this.entityData.get(FLEEING);
    }


    // =========================================================
    // ANIMACIÓN DE COMER
    // =========================================================

    public void playEatAnim() {
        triggerAnim(
                "special",
                "eat"
        );
    }


    // =========================================================
    // LANA
    // =========================================================

    /**
     * Devuelve el estado completo de la lana.
     *
     * 0 = toda la lana presente
     * 31 = todas las partes cortadas
     */
    public int getWoolState() {
        return this.entityData.get(WOOL_STATE);
    }


    /**
     * Comprueba si una determinada parte de lana está cortada.
     *
     * part:
     * 0 = lana
     * 1 = lana2
     * 2 = lana3
     * 3 = lana4
     * 4 = lana5
     */
    public boolean isWoolPartCut(int part) {

        if (part < 0 || part > 4) {
            return false;
        }

        int bit = 1 << part;

        return (getWoolState() & bit) != 0;
    }


    /**
     * Comprueba si todavía queda al menos una parte de lana.
     */
    public boolean hasWool() {
        return getWoolState() != ALL_WOOL_CUT;
    }


    /**
     * Comprueba si existe alguna parte de lana que haya sido cortada.
     */
    public boolean hasAnyCutWool() {
        return getWoolState() != 0;
    }


    /**
     * Corta una parte específica de lana.
     *
     * Devuelve true si realmente se pudo cortar.
     */
    public boolean shearWoolPart(int part) {

        // Los bebés no tienen lana que cortar.
        if (this.isBaby()) {
            return false;
        }

        if (part < 0 || part > 4) {
            return false;
        }

        int bit = 1 << part;

        int currentState = getWoolState();

        // Ya estaba cortada.
        if ((currentState & bit) != 0) {
            return false;
        }

        this.entityData.set(
                WOOL_STATE,
                currentState | bit
        );

        return true;
    }


    /**
     * Corta una parte de lana aleatoria que todavía esté presente.
     *
     * Devuelve:
     *
     * 0 = lana
     * 1 = lana2
     * 2 = lana3
     * 3 = lana4
     * 4 = lana5
     *
     * -1 = no se pudo cortar ninguna.
     */
    public int shearRandomWoolPart() {

        if (this.isBaby()) {
            return -1;
        }

        int currentState = getWoolState();

        // Toda la lana ya fue cortada.
        if (currentState == ALL_WOOL_CUT) {
            return -1;
        }

        int[] availableParts = new int[5];
        int availableCount = 0;

        for (int part = 0; part < 5; part++) {

            int bit = 1 << part;

            if ((currentState & bit) == 0) {
                availableParts[availableCount++] = part;
            }
        }

        if (availableCount <= 0) {
            return -1;
        }

        int selectedPart =
                availableParts[
                        this.random.nextInt(availableCount)
                        ];

        shearWoolPart(selectedPart);

        return selectedPart;
    }


    /**
     * Regenera toda la lana que haya sido cortada.
     *
     * Los bebés no regeneran lana porque no tienen lana.
     */
    public void regenerateWool() {

        if (this.isBaby()) {
            return;
        }

        if (getWoolState() != 0) {
            this.entityData.set(
                    WOOL_STATE,
                    0
            );
        }
    }


    // =========================================================
    // GUARDADO DE LA LANA
    // =========================================================

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putInt(
                "WoolState",
                getWoolState()
        );
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        if (tag.contains(
                "WoolState",
                Tag.TAG_INT
        )) {
            this.entityData.set(
                    WOOL_STATE,
                    tag.getInt("WoolState")
            );
        }
    }


    // =========================================================
    // GOALS
    // =========================================================

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                1,
                new PanicGoal(
                        this,
                        1.5
                )
        );

        this.goalSelector.addGoal(
                2,
                new BreedGoal(
                        this,
                        1.0
                )
        );

        this.goalSelector.addGoal(
                3,
                new TemptGoal(
                        this,
                        1.15,
                        Ingredient.of(Items.WHEAT),
                        false
                )
        );

        this.goalSelector.addGoal(
                4,
                new FollowParentGoal(
                        this,
                        1.1
                )
        );

        this.goalSelector.addGoal(
                5,
                new WoolodonEatGrassGoal(this)
        );

        this.goalSelector.addGoal(
                6,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        1.0
                )
        );

        this.goalSelector.addGoal(
                7,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        6.0F
                )
        );

        this.goalSelector.addGoal(
                8,
                new RandomLookAroundGoal(this)
        );
    }


    // =========================================================
    // ATRIBUTOS
    // =========================================================

    public static AttributeSupplier.Builder createAttributes() {

        return Animal.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        20.0
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.22
                );
    }


    // =========================================================
    // ALIMENTO
    // =========================================================

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }


    // =========================================================
    // CRÍA
    // =========================================================

    @Override
    public AgeableMob getBreedOffspring(
            ServerLevel level,
            AgeableMob mate
    ) {

        return net.tucas.sculkeritegreatsword.init.ModEntities
                .WOOLODON
                .get()
                .create(level);
    }


    // =========================================================
    // DAÑO / HUIDA
    // =========================================================

    @Override
    public boolean hurt(
            DamageSource source,
            float amount
    ) {

        boolean result =
                super.hurt(
                        source,
                        amount
                );

        if (
                result &&
                        !level().isClientSide
        ) {

            this.fleeTicks =
                    FLEE_DURATION;

            this.entityData.set(
                    FLEEING,
                    true
            );
        }

        return result;
    }


    // =========================================================
    // TICK
    // =========================================================

    @Override
    public void aiStep() {

        super.aiStep();

        if (!level().isClientSide) {

            // Cooldown de comer
            if (eatCooldown > 0) {
                eatCooldown--;
            }

            // Tiempo de huida
            if (fleeTicks > 0) {

                fleeTicks--;

                if (fleeTicks <= 0) {

                    fleeTicks = 0;

                    this.entityData.set(
                            FLEEING,
                            false
                    );
                }
            }
        }
    }


    // =========================================================
    // ANIMACIONES GECKOLIB
    // =========================================================

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar registrar
    ) {

        registrar.add(
                new AnimationController<>(
                        this,
                        "movement",
                        5,
                        state -> {

                            AnimationController<?> specialController =
                                    this.getAnimatableInstanceCache()
                                            .getManagerForId(this.getId())
                                            .getAnimationControllers()
                                            .get("special");

                            if (
                                    specialController != null &&
                                            specialController.isPlayingTriggeredAnimation()
                            ) {

                                return PlayState.STOP;
                            }

                            if (state.isMoving()) {

                                return state.setAndContinue(
                                        this.isFleeing()
                                                ? RUN
                                                : WALK
                                );
                            }

                            return state.setAndContinue(
                                    IDLE
                            );
                        }
                )
        );


        registrar.add(
                new AnimationController<>(
                        this,
                        "special",
                        0,
                        state -> PlayState.STOP
                ).triggerableAnim(
                        "eat",
                        EAT
                )
        );
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


    // =========================================================
    // HITBOX / ESCALA
    // =========================================================

    @Override
    public EntityDimensions getDimensions(
            Pose pose
    ) {

        EntityDimensions base =
                super.getDimensions(pose);

        return this.isBaby()
                ? base.scale(0.5f)
                : base;
    }


    @Override
    public float getScale() {

        return this.isBaby()
                ? 0.5F
                : 1.0F;
    }
}
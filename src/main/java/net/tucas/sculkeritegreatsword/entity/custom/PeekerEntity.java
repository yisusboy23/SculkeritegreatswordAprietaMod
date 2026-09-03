package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;
import net.tucas.sculkeritegreatsword.entity.ai.goal.PeekerAvoidPlayerGoal;
import net.tucas.sculkeritegreatsword.entity.ai.goal.PeekerConstants;
import net.tucas.sculkeritegreatsword.init.ModEntities;         // <-- ajusta al nombre real
import software.bernie.geckolib.animatable.GeoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BonemealableBlock;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.item.Moditems;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Peeker: nace bebé, crece a adulto solo (usa el sistema de edad de Animal, no hace
 * falta reinventarlo), huye del jugador mientras es salvaje, se puede domesticar y,
 * una vez domesticado, ensillar y montar.
 *
 * Diseño: UNA sola clase de entidad (no una "PeekerEntity" + "PeekerSaddledEntity"
 * duplicada). El estado de silla es solo un booleano sincronizado, igual que hace
 * el juego base con el cerdo o el caballo. Menos código, menos duplicación, y el
 * modelo/renderer pueden decidir la textura leyendo isSaddled()/isTame() en vez de
 * necesitar dos modelos distintos para lo mismo.
 */
public class PeekerEntity extends TamableAnimal implements GeoEntity {

    private static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(PeekerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(PeekerEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PeekerEntity(EntityType<? extends PeekerEntity> type, Level world) {
        super(type, world);
        this.setSpeed(0.3F);
        this.setMaxUpStep(PeekerConstants.MAX_STEP_HEIGHT);
    }

    public PeekerEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.PEEKER.get(), world);
    }

    // ------------------------------------------------------------------
    // Datos sincronizados / guardado
    // ------------------------------------------------------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE, PeekerConstants.TEXTURE_WILD);
        this.entityData.define(SADDLED, false);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public boolean isSaddled() {
        return this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
        refreshTextureForCurrentState();
    }

    /** Mantiene la textura sincronizada con tame/saddle sin que nadie tenga que acordarse de hacerlo a mano. */
    private void refreshTextureForCurrentState() {
        setTexture(isSaddled() ? PeekerConstants.TEXTURE_SADDLED : PeekerConstants.TEXTURE_WILD);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Texture", getTexture());
        compound.putBoolean("Saddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Texture")) setTexture(compound.getString("Texture"));
        if (compound.contains("Saddled")) this.entityData.set(SADDLED, compound.getBoolean("Saddled"));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ------------------------------------------------------------------
    // IA
    // ------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PeekerAvoidPlayerGoal(this,
                PeekerConstants.AVOID_PLAYER_DISTANCE, PeekerConstants.AVOID_PLAYER_SPEED_WALK, PeekerConstants.AVOID_PLAYER_SPEED_SPRINT));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, PeekerConstants.LOOK_AT_PLAYER_RANGE));
        this.goalSelector.addGoal(3, new PanicGoal(this, PeekerConstants.PANIC_SPEED));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, PeekerConstants.STROLL_SPEED));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new FloatGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, PeekerConstants.MOVEMENT_SPEED)
                .add(Attributes.MAX_HEALTH, PeekerConstants.MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE, PeekerConstants.FOLLOW_RANGE)
                .add(Attributes.ATTACK_KNOCKBACK, PeekerConstants.ATTACK_KNOCKBACK);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    // ------------------------------------------------------------------
    // Sonidos
    // ------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return modSound(this.isBaby() ? PeekerConstants.SOUND_IDLE_BABY : PeekerConstants.SOUND_IDLE);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return modSound(this.isBaby() ? PeekerConstants.SOUND_HURT_BABY : PeekerConstants.SOUND_HURT);
    }

    private SoundEvent modSound(String name) {
        // Sustituye por tu propio RegistryObject<SoundEvent> cuando lo tengas, ej: ModSounds.PEEKER_IDLE.get()
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Sculkeritegreatsword.MOD_ID, name));
    }

    // ------------------------------------------------------------------
    // Domesticación + silla + montar (todo pasa por mobInteract)
    // ------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            return tryTame(player, stack);
        }

        if (!this.isSaddled() && isSaddleItem(stack)) {
            return equipSaddle(player, stack);
        }

        if (this.isSaddled() && player.isShiftKeyDown() && stack.isEmpty()) {
            return unequipSaddle(player);
        }

        if (this.isSaddled() && !this.isVehicle() && hand == InteractionHand.MAIN_HAND && stack.isEmpty()) {
            return mount(player);
        }

        return super.mobInteract(player, hand);
    }

    /**
     * Punto de extensión: define aquí con qué ítem se domestica al Peeker
     * (por ahora placeholder, me dijiste que lo confirmas después).
     */
    protected boolean isTameItem(ItemStack stack) {
        return stack.is(Items.BONE_MEAL);
    }

    private InteractionResult tryTame(Player player, ItemStack stack) {
        if (!isTameItem(stack)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (this.random.nextInt(PeekerConstants.TAME_CHANCE_ONE_IN) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.level().broadcastEntityEvent(this, (byte) 7); // partículas de corazones vanilla
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6); // partículas de humo vanilla (falló)
        }
        return InteractionResult.SUCCESS;
    }

    private boolean isSaddleItem(ItemStack stack) {
        return stack.is(Items.SADDLE);
    }

    private InteractionResult equipSaddle(Player player, ItemStack stack) {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        setSaddled(true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult unequipSaddle(Player player) {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        setSaddled(false);
        this.spawnAtLocation(Items.SADDLE);
        this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult mount(Player player) {
        if (!this.level().isClientSide) {
            this.navigation.stop();
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // ------------------------------------------------------------------
    // Movimiento cuando lleva jinete
    // ------------------------------------------------------------------

    @Override
    public boolean isPushedByFluid() {
        return !this.isVehicle();
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() + PeekerConstants.SADDLE_RIDER_Y_OFFSET;
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        LivingEntity rider = this.getControllingPassenger();
        if (this.isAlive() && this.isVehicle() && this.isSaddled() && rider != null) {
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(rider.getXRot() * PeekerConstants.SADDLED_RIDER_PITCH_FACTOR);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float forward = rider.zza;
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new net.minecraft.world.phys.Vec3(0.0D, 0.0D, forward));
        } else {
            this.setSpeed(0.0F);
            super.travel(travelVector);
        }
    }

    /**
     * FIX: al bajarse el jinete, la velocidad horizontal residual (deltaMovement)
     * seguía decayendo de a poco durante varios ticks, lo que hacía que GeckoLib
     * detectara "isMoving" por encima del umbral y se quedara pegado en la
     * animación de walk en vez de pasar a idle al toque. Cortamos X/Z a 0 apenas
     * se desmonta, dejando Y intacto para no alterar la caída/gravedad.
     */
    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        // FIX real: no era solo velocidad residual. El goalSelector (RandomStrollGoal,
        // PanicGoal, etc.) sigue vivo mientras el jugador te monta, y su navegación
        // puede quedar con un camino "cargado" que travel() ignora mientras hay
        // jinete. Al desmontar, travel() vuelve a ejecutar ese camino pendiente y
        // el Peeker sale caminando de verdad varios segundos, no es un bug visual
        // de GeckoLib. Cortamos la navegación en ambos extremos (montar y
        // desmontar) para que no quede nada pendiente.
        this.navigation.stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        this.setSpeed(0.0F);
        // FIX real, sacado de comparar con la versión vieja (MCreator) del mod:
        // esa versión alimentaba a mano el walkAnimation de Vanilla SOLO mientras
        // estaba montada, y lo soltaba al desmontar dejando que Mob.aiStep() lo
        // recalculara solo. walkAnimation.stop() es el método de Vanilla hecho
        // justo para cortar la animación de caminar al instante (lo usan
        // minecarts/botes al desenganchar el jinete).
        // walkAnimation.stop() no existe en esta versión de Mappings (1.20.1).
        // update(objetivo, factorDeSuavizado) con factor=1.0F aplica el cambio
        // al 100% en un solo tick en vez de ir decayendo de a poco: le decimos
        // "el movimiento objetivo es 0, y aplicalo YA, sin lerp".
        this.walkAnimation.update(0.0F, 1.0F);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return this.isSaddled() && passenger instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return this.isVehicle() && super.isControlledByLocalInstance();
    }

    // ------------------------------------------------------------------
    // Muerte / partículas de baja vida
    // ------------------------------------------------------------------

    @Override
    public void die(DamageSource source) {
        super.die(source);
        // El resto de la secuencia (daño, partículas, drops, textura) ocurre en tickDeath()
    }

    @Override
    public void aiStep() {
        super.aiStep();
        tickLowHealthEffects();
    }

    private void tickLowHealthEffects() {
        if (this.getHealth() > PeekerConstants.LOW_HEALTH_THRESHOLD) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        RandomSource random = serverLevel.getRandom();
        if (random.nextInt(PeekerConstants.LOW_HEALTH_CHANCE_ONE_IN) != 0) return;

        int count = PeekerConstants.LOW_HEALTH_PARTICLE_MIN + random.nextInt(PeekerConstants.LOW_HEALTH_PARTICLE_EXTRA);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 3.0D, this.getZ(), count, 0.5D, 0.5D, 0.5D, 0.02D);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;

        // Tick 2: la textura cambia a "explotando" casi de inmediato, como en el original
        if (this.deathTime == 2) {
            setTexture(this.isSaddled()
                    ? PeekerConstants.TEXTURE_EXPLODE_SADDLED
                    : PeekerConstants.TEXTURE_EXPLODE_WILD);
        }

        // Tick 20: posibilidad de soltar el fetus (25%, igual que el original)
        if (this.deathTime == 20 && this.level() instanceof ServerLevel serverLevel) {
            if (this.random.nextInt(4) == 0) {
                ItemEntity fetusEntity = new ItemEntity(serverLevel,
                        this.getX(), this.getY(), this.getZ(),
                        new ItemStack(Moditems.PEEKER_FETUS.get()));
                fetusEntity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(fetusEntity);
            }
        }

        // Tick 25: explosión completa (daño de área + partículas + drops)
        if (this.deathTime == PeekerConstants.DEATH_ANIMATION_TICKS) {
            if (this.level() instanceof ServerLevel serverLevel) {
                explodeEffects(serverLevel);
            }
            this.remove(Entity.RemovalReason.KILLED);
            SoundEvent explode = modSound(PeekerConstants.SOUND_EXPLODE);
            if (explode != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), explode, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    private void explodeEffects(ServerLevel level) {
        RandomSource random = level.getRandom();

        int explosionCount = PeekerConstants.EXPLOSION_PARTICLE_MIN + random.nextInt(PeekerConstants.EXPLOSION_PARTICLE_EXTRA);
        int critCount = PeekerConstants.CRIT_PARTICLE_MIN + random.nextInt(PeekerConstants.CRIT_PARTICLE_EXTRA);
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), explosionCount, 1.0D, 1.0D, 1.0D, 0.0D);
        level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(), critCount, 1.0D, 1.0D, 1.0D, 0.1D);

        AABB damageBox = this.getBoundingBox().inflate(PeekerConstants.EXPLOSION_DAMAGE_RADIUS);
        List<Entity> nearby = level.getEntities(this, damageBox, e -> e.isAlive() && !(e instanceof ItemEntity));
        for (Entity entity : nearby) {
            float damage = PeekerConstants.EXPLOSION_DAMAGE_MIN
                    + random.nextFloat() * (PeekerConstants.EXPLOSION_DAMAGE_MAX - PeekerConstants.EXPLOSION_DAMAGE_MIN);
            entity.hurt(this.damageSources().explosion(null, null), damage);
        }
        growNearbyGrass(level, random); // ahora se llama UNA sola vez, no por cada entidad dañada

        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    private void growNearbyGrass(ServerLevel level, RandomSource random) {
        BlockPos center = this.blockPosition();
        int attempts = 10 + random.nextInt(6); // 10-15 intentos, como el original

        for (int i = 0; i < attempts; i++) {
            BlockPos pos = center.offset(
                    random.nextInt(3) - 1,
                    -1,
                    random.nextInt(3) - 1
            );
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof BonemealableBlock bonemealable
                    && bonemealable.isValidBonemealTarget(level, pos, state, level.isClientSide())) {
                bonemealable.performBonemeal(level, random, pos, state);
            }
        }
    }

    // ------------------------------------------------------------------
    // Bebé -> adulto (gratis, usando el sistema de Animal) + cría
    // ------------------------------------------------------------------

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions adult = super.getDimensions(pose).scale(PeekerConstants.BASE_ENTITY_SCALE);
        return this.isBaby() ? adult.scale(0.5F) : adult;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageable) {
        PeekerEntity baby = ModEntities.PEEKER.get().create(serverLevel);
        if (baby != null) {
            baby.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(baby.blockPosition()), MobSpawnType.BREEDING, null, null);
        }
        return baby;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false; // define aquí el ítem de cría si lo vas a usar más adelante
    }

    // ------------------------------------------------------------------
    // GeckoLib
    // ------------------------------------------------------------------

    private PlayState movementPredicate(AnimationState<PeekerEntity> event) {
        if (this.isDeadOrDying()) {
            return event.setAndContinue(RawAnimation.begin().thenPlay(PeekerConstants.ANIM_EXPLODE));
        }

        // FIX real: no medimos "velocidad intentada" (deltaMovement / event.isMoving()),
        // porque eso puede ser distinto de cero incluso trabado contra una pared o
        // con el rider empujando "adelante" sin que la posición cambie un milímetro
        // (de ahí el "hace walk y no avanza nada"). Medimos DESPLAZAMIENTO REAL:
        // comparamos la posición actual contra la del tick anterior (xo/zo, que
        // Vanilla ya trackea). Esto es exactamente lo mismo que usa
        // Entity#calculateEntityAnimation internamente, y es inmune a colisiones,
        // input del jinete, o rarezas de GeckoLib.
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        double distMovedSqr = dx * dx + dz * dz;
        boolean actuallyMoving = distMovedSqr > 0.0009D; // se movió más de ~0.03 bloques este tick

        if (actuallyMoving) {
            return event.setAndContinue(RawAnimation.begin().thenLoop(PeekerConstants.ANIM_WALK));
        }

        // No usamos thenLoop(IDLE) acá a propósito: GeckoLib tiene un bug conocido
        // (github.com/bernie-g/geckolib/issues/65) donde la transición DESDE una
        // animación en movimiento HACIA otra queda pegada varios segundos sin
        // importar transitionLengthTicks. Devolver PlayState.STOP usa un camino
        // interno distinto que sí corta al instante, y luego forzamos idle en el
        // siguiente frame de todas formas.
        if (event.getController().getAnimationState() == software.bernie.geckolib.core.animation.AnimationController.State.STOPPED) {
            return event.setAndContinue(RawAnimation.begin().thenLoop(PeekerConstants.ANIM_IDLE));
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        // OJO: este valor puede ser TICKS o SEGUNDOS según tu versión de GeckoLib.
        // Si "5" te daba ~5 segundos de retraso walk->idle, tu versión lo toma como
        // segundos. Usá algo bajo (ej. 0 para corte instantáneo, o 0.1-0.2 si querés
        // un mini-crossfade). Si tu versión SÍ es en ticks, podés subirlo a 2-4.
        data.add(new AnimationController<>(this, "movement", 0, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
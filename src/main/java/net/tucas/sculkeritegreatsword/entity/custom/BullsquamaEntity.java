package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tucas.sculkeritegreatsword.item.Moditems;
import net.tucas.sculkeritegreatsword.entity.ai.goal.BullsquamaEatGoal;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class BullsquamaEntity extends Animal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation LICK = RawAnimation.begin().thenPlay("lick");
    private static final RawAnimation SPIT = RawAnimation.begin().thenPlay("spit");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean hunting = false;
    private int eatCooldown = 0;

    private final List<ItemStack> storedLoot = new ArrayList<>();

    public BullsquamaEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public boolean isHunting() { return hunting; }
    public void setHunting(boolean hunting) { this.hunting = hunting; }
    public int getEatCooldown() { return eatCooldown; }
    public void setEatCooldown(int eatCooldown) { this.eatCooldown = eatCooldown; }

    public void storeLoot(List<ItemStack> loot) {
        storedLoot.clear();
        storedLoot.addAll(loot);
    }

    public List<ItemStack> getStoredLoot() {
        return storedLoot;
    }

    public void playLickAnim() {
        triggerAnim("special", "lick");
    }

    public void playSpitAnim() {
        triggerAnim("special", "spit");
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0,new FloatGoal(this));
        this.goalSelector.addGoal(1,new BullsquamaEatGoal(this));
        this.goalSelector.addGoal(2,new MeleeAttackGoal(this,1.2,true));
        this.goalSelector.addGoal(3,new BreedGoal(this,1.0));
        this.goalSelector.addGoal(4,new FollowParentGoal(this,1.1));
        this.goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,1.0));
        this.goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,6F));
        this.goalSelector.addGoal(7,new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1,new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH,60)
                .add(Attributes.MOVEMENT_SPEED,0.25)
                .add(Attributes.ATTACK_DAMAGE,18);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SLIME_BALL);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {

        if (!level.isClientSide) {
            this.spawnAtLocation(new ItemStack(Moditems.BULLSQUAMA_EGG.get()));
        }

        this.setAge(6000);
        mate.setAge(6000);

        this.setInLoveTime(0);
        ((Animal) mate).setInLoveTime(0);

        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(stack.is(Items.SPIDER_EYE) && !this.isBaby()) {
            if(!level().isClientSide) {
                setHunting(true);

                if(!player.isCreative()) {
                    stack.shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if(!level().isClientSide && eatCooldown > 0) {
            eatCooldown--;
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions base = super.getDimensions(pose);
        return this.isBaby() ? base.scale(0.8f) : base;
    }

    @Override
    public float getScale() {
        return this.isBaby() ? 0.5f : 1f;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {

        registrar.add(new AnimationController<>(this,"movement",5,state -> {

            AnimationController<?> specialController =
                    this.getAnimatableInstanceCache()
                            .getManagerForId(this.getId())
                            .getAnimationControllers()
                            .get("special");

            if(specialController != null && specialController.isPlayingTriggeredAnimation()) {
                return PlayState.STOP;
            }

            if(state.isMoving()) {
                return state.setAndContinue(WALK);
            }

            return state.setAndContinue(IDLE);
        }));


        registrar.add(new AnimationController<>(this,"attack",0,state -> {

            if(this.swinging) {
                return state.setAndContinue(ATTACK);
            }

            return PlayState.STOP;
        }));


        registrar.add(
                new AnimationController<>(this,"special",0,state -> PlayState.STOP)

                        .triggerableAnim("lick",LICK)

                        .triggerableAnim("spit",SPIT)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
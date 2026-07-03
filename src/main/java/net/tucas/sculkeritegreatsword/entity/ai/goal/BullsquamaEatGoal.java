package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.BullsquamaEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BullsquamaEatGoal extends Goal {

    private final BullsquamaEntity bullsquama;

    private LivingEntity prey;

    private int timer = 0;

    private enum State {
        MOVE,
        LICK,
        DIGEST,
        SPIT
    }

    private State state = State.MOVE;

    public BullsquamaEatGoal(BullsquamaEntity bullsquama) {
        this.bullsquama = bullsquama;

        this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        if(!bullsquama.isHunting()) return false;
        if(bullsquama.getEatCooldown() > 0) return false;

        prey = findNearestPrey();

        return prey != null;
    }

    @Override
    public boolean canContinueToUse() {
        return bullsquama.isHunting();
    }

    @Override
    public void start() {
        state = State.MOVE;
        timer = 0;
    }

    @Override
    public void tick() {

        switch(state) {

            case MOVE -> {

                if(prey == null || !prey.isAlive()) {
                    bullsquama.setHunting(false);
                    return;
                }

                bullsquama.getNavigation().moveTo(prey,1.1);

                bullsquama.getLookControl().setLookAt(
                        prey,
                        30.0F,
                        30.0F
                );

                if(bullsquama.distanceTo(prey) <= 3) {

                    bullsquama.getNavigation().stop();

                    bullsquama.playLickAnim();

                    timer = 30;

                    state = State.LICK;
                }
            }

            case LICK -> {

                timer--;

                bullsquama.getNavigation().stop();
                bullsquama.setDeltaMovement(Vec3.ZERO);

                if(prey != null && prey.isAlive()) {

                    bullsquama.getLookControl().setLookAt(prey, 30F, 30F);

                    Vec3 mouth = new Vec3(
                            bullsquama.getX(),
                            bullsquama.getY()+1,
                            bullsquama.getZ()
                    );

                    Vec3 lerped = prey.position().lerp(mouth,0.25);

                    prey.teleportTo(
                            lerped.x,
                            lerped.y,
                            lerped.z
                    );
                }

                // MATAR A MITAD DE LA ANIMACION
                if(timer == 15) {

                    if(prey != null && prey.isAlive()) {

                        collectLoot(prey);

                        prey.hurt(
                                bullsquama.damageSources().mobAttack(bullsquama),
                                9999
                        );
                    }
                }

                // TERMINAR ANIMACION COMPLETA
                if(timer <= 0) {

                    timer = 100;

                    state = State.DIGEST;
                }
            }

            case DIGEST -> {

                timer--;

                if(timer <= 0) {

                    bullsquama.playSpitAnim();

                    timer = 30;

                    state = State.SPIT;
                }
            }

            case SPIT -> {

                timer--;

                if(timer == 15) {

                    for(ItemStack stack : bullsquama.getStoredLoot()) {

                        ItemStack doubled = stack.copy();

                        doubled.setCount(doubled.getCount()*2);

                        bullsquama.level().addFreshEntity(
                                new ItemEntity(
                                        bullsquama.level(),
                                        bullsquama.getX(),
                                        bullsquama.getY()+1,
                                        bullsquama.getZ(),
                                        doubled
                                )
                        );
                    }
                }

                if(timer <= 0) {


                    bullsquama.setHunting(false);

                    bullsquama.setEatCooldown(200);
                }
            }
        }
    }

    @Override
    public void stop() {


        bullsquama.setHunting(false);
    }

    private void collectLoot(LivingEntity entity) {

        if(!(bullsquama.level() instanceof ServerLevel serverLevel)) return;

        var lootTable =
                serverLevel.getServer()
                        .getLootData()
                        .getLootTable(entity.getType().getDefaultLootTable());

        var params =
                new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .withParameter(
                                net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,
                                entity
                        )
                        .withParameter(
                                net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                                entity.position()
                        )
                        .withParameter(
                                net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,
                                bullsquama.damageSources().mobAttack(bullsquama)
                        )
                        .create(
                                net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY
                        );

        List<ItemStack> drops = new ArrayList<>();

        lootTable.getRandomItemsRaw(params,drops::add);

        bullsquama.storeLoot(drops);
    }

    private LivingEntity findNearestPrey() {

        List<LivingEntity> nearby =
                bullsquama.level().getEntitiesOfClass(
                        LivingEntity.class,
                        bullsquama.getBoundingBox().inflate(15)
                );

        LivingEntity closest = null;

        double dist = Double.MAX_VALUE;

        for(LivingEntity entity : nearby) {

            if(entity == bullsquama) continue;
            if(entity instanceof net.minecraft.world.entity.player.Player) continue;
            if(entity instanceof BullsquamaEntity) continue;

            double d = bullsquama.distanceToSqr(entity);

            if(d < dist) {
                dist = d;
                closest = entity;
            }
        }

        return closest;
    }
}
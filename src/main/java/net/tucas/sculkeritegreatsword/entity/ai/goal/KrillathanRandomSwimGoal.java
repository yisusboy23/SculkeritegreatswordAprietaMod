package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class KrillathanRandomSwimGoal extends Goal {

    private BlockPos targetPos = null;
    private final Mob mob;
    private final double speedModifier;
    private final int range;
    private final int chance;
    private final int belowSeaLevel;

    // speed      → velocidad de nado (1.0 = normal)
    // chance     → 1 entre X ticks intentará moverse (20 = cada segundo aprox)
    // range      → radio en bloques donde busca destino
    // belowSeaLevel → cuántos bloques bajo la superficie nada máximo
    public KrillathanRandomSwimGoal(Mob mob, double speed, int chance, int range, int belowSeaLevel) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.mob = mob;
        this.speedModifier = speed;
        this.chance = chance;
        this.range = range;
        this.belowSeaLevel = belowSeaLevel;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return mob.isInWaterOrBubble()
                && (target == null || !target.isAlive())
                && (chance == 0 || mob.getRandom().nextInt(chance) == 0);
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null
                && mob.distanceToSqr(Vec3.atCenterOf(targetPos)) > 30
                && !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.targetPos = this.findSwimToPos();
        this.mob.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
    }

    // Comprueba si hay línea de visión libre hasta el destino
    private boolean isTargetBlocked(Vec3 target) {
        Vec3 eye = new Vec3(mob.getX(), mob.getEyeY(), mob.getZ());
        return mob.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob))
                .getType() != HitResult.Type.MISS;
    }

    // Busca una posición válida dentro del agua, sin obstáculos y bajo la superficie
    private BlockPos findSwimToPos() {
        BlockPos around = mob.blockPosition();

        // Sube hasta encontrar la superficie del agua
        BlockPos.MutableBlockPos move = new BlockPos.MutableBlockPos();
        move.set(mob.getX(), mob.getY(), mob.getZ());
        while (move.getY() < mob.level().getMaxBuildHeight()
                && mob.level().getFluidState(move).is(FluidTags.WATER)) {
            move.move(0, 5, 0);
        }
        int surfaceY = move.getY();

        // El mob no nada más arriba de (superficie - belowSeaLevel)
        around = around.atY(Math.min(surfaceY - belowSeaLevel, around.getY()));

        // Intenta 15 posiciones aleatorias dentro del rango
        for (int i = 0; i < 15; i++) {
            BlockPos candidate = around.offset(
                    mob.getRandom().nextInt(range) - range / 2,
                    mob.getRandom().nextInt(range) - range / 2,
                    mob.getRandom().nextInt(range) - range / 2
            );
            if (mob.level().getFluidState(candidate).is(FluidTags.WATER)
                    && !this.isTargetBlocked(Vec3.atCenterOf(candidate))
                    && candidate.getY() > mob.level().getMinBuildHeight() + 1) {
                return candidate;
            }
        }
        return around;
    }
}
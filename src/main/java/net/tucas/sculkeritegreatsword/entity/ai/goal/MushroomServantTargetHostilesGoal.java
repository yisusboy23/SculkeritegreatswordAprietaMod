package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;

public class MushroomServantTargetHostilesGoal extends NearestAttackableTargetGoal<LivingEntity> {

    private final Mob mob;

    public MushroomServantTargetHostilesGoal(Mob mob) {
        // ⭐ ATACA A TODAS LAS ENTIDADES QUE IMPLEMENTEN Enemy (todos los hostiles)
        // intervalo = 5 → Busca cada 5 ticks
        // mustSee = false → Detecta a través de paredes
        // mustReach = false → No verifica pathfinding
        super(mob, LivingEntity.class, 5, false, false,
                (entity) -> entity instanceof Enemy); // ⭐ Filtra solo enemigos
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    protected double getFollowDistance() {
        // ⭐ RANGO DE DETECCIÓN: 15 bloques
        return 15.0D;
    }
}
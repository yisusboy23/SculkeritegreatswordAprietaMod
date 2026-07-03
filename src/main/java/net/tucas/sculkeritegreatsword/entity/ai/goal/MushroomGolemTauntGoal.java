package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomGolemEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal que hace que el golem atraiga a TODOS los enemigos hacia él
 * Sistema de agro constante para proteger al dueño
 */
public class MushroomGolemTauntGoal extends Goal {
    private final MushroomGolemEntity golem;
    private int tauntCooldown = 0;
    private static final int TAUNT_RADIUS = 20;
    private static final int TAUNT_INTERVAL = 20; // Cada segundo

    public MushroomGolemTauntGoal(MushroomGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        // Activo mientras esté domesticado, vivo y NO huyendo
        return golem.isTame() && golem.isAlive() && !golem.isRetreating();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        tauntCooldown--;

        if (tauntCooldown <= 0) {
            performTaunt();
            tauntCooldown = TAUNT_INTERVAL;
        }
    }

    private void performTaunt() {
        AABB area = new AABB(golem.blockPosition()).inflate(TAUNT_RADIUS);
        List<Mob> mobs = golem.level().getEntitiesOfClass(Mob.class, area);

        for (Mob mob : mobs) {
            // Redirigir TODOS los enemigos que atacan a jugadores hacia el golem
            if (mob instanceof Enemy && mob.getTarget() instanceof Player) {
                mob.setTarget(golem);
            }
        }

        // Efecto de sonido cada 3 segundos
        if (golem.tickCount % 60 == 0) {
            golem.level().playSound(null, golem.blockPosition(),
                    SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.NEUTRAL, 0.5F, 1.0F);
        }
    }
}

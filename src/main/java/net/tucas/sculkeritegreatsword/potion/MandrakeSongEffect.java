package net.tucas.sculkeritegreatsword.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tucas.sculkeritegreatsword.entity.custom.MandrakeEntity;

public class MandrakeSongEffect extends MobEffect {

    // Duración pensada para el jugador: 2 minutos = 2400 ticks
    public static final int PLAYER_DURATION_TICKS = 2400;

    // Cada cuántos ticks se refresca nausea/oscuridad y se aplica daño a mobs
    private static final int TICK_INTERVAL = 20; // 1 vez por segundo

    // Por encima de esta vida máxima, el efecto no hace absolutamente nada
    private static final float MOB_HEALTH_THRESHOLD = 150.0F;

    // Constante de escalado cuadrático para mobs (AJUSTABLE a tu gusto)
    // damage = maxHealth^2 * MOB_DAMAGE_SCALE
    private static final float MOB_DAMAGE_SCALE = 1f / 500f;

    public MandrakeSongEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Se evalúa cada TICK_INTERVAL ticks, y siempre en el último tick de vida (duration == 1)
        return duration % TICK_INTERVAL == 0 || duration == 1;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            handlePlayer(player);
        } else {
            handleMob(livingEntity);
        }
    }

    private void handlePlayer(Player player) {
        MobEffectInstance instance = player.getEffect(this);
        if (instance == null) return;

        // Refrescar nausea + oscuridad mientras dure la maldición
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, TICK_INTERVAL + 5, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, TICK_INTERVAL + 5, 0, false, false, false));

        // Si llegamos al último tick sin haber tomado leche (que quita el efecto), mata al jugador
        if (instance.getDuration() <= 1) {
            player.hurt(player.damageSources().magic(), 10000.0F);
        }
    }

    private void handleMob(LivingEntity mob) {
        // Las mandrágoras son inmunes a su propio canto y al de otras mandrágoras
        if (mob instanceof MandrakeEntity) {
            mob.removeEffect(this);
            return;
        }

        float maxHealth = mob.getMaxHealth();

        // Inmunidad total por encima del umbral
        if (maxHealth > MOB_HEALTH_THRESHOLD) {
            mob.removeEffect(this);
            return;
        }

        // Mismo "canto" también los marea/enceguece
        mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION, TICK_INTERVAL + 5, 0, false, false, false));
        mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS, TICK_INTERVAL + 5, 0, false, false, false));

        // Daño cuadrático: a más vida máxima, proporcionalmente MÁS castigo
        float damage = (maxHealth * maxHealth) * MOB_DAMAGE_SCALE;
        mob.hurt(mob.damageSources().magic(), damage);
    }
}
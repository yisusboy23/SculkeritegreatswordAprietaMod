package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

import java.util.Optional;

/** Priority 5: rango medio/lejano. Alterna al azar entre Cannon Shot y Beam. */
public class BossRangedAttackGoal extends AbstractBossGoal {
    private static final int CANNON_FIRE_TICK = 21;
    private static final double BEAM_RANGE = 20.0D;
    private static final float BEAM_DAMAGE = 5.0F;
    private static final int BEAM_HIT_INTERVAL = 10;
    private static final double BEAM_TRACKING = 0.06D;   // <1: el rayo "persigue" con retardo => se puede esquivar
    private static final int SHIELD_DISABLE_TICKS = 160;
    private static final int COOLDOWN = 50;

    private boolean finished;
    private int phaseTicks;
    private Vec3 aim = Vec3.ZERO;

    public BossRangedAttackGoal(ForgottenConstructEntity boss) { super(boss); }

    @Override
    public boolean canUse() {
        if (!boss.isAwake() || boss.getBossState() != BossState.IDLE
                || boss.getRangedCooldown() > 0 || !hasValidTarget()) return false;
        LivingEntity t = boss.getTarget();
        double d = boss.distanceToSqr(t);
        double melee = ForgottenConstructEntity.MELEE_RANGE, max = ForgottenConstructEntity.RANGED_MAX_RANGE;
        return d > melee * melee && d <= max * max && boss.hasLineOfSight(t);
    }

    @Override public boolean canContinueToUse() { return !finished && boss.getBossState().isRanged(); }

    @Override
    public void start() {
        finished = false;
        LivingEntity t = boss.getTarget();
        aim = t != null ? t.getEyePosition().subtract(boss.getEyePosition()).normalize() : boss.getLookAngle();
        enter(boss.getRandom().nextBoolean() ? BossState.BEAM_START : BossState.CANNON_SHOT);
    }

    private void enter(BossState s) { boss.setBossState(s); phaseTicks = 0; }

    @Override
    public void tick() {
        LivingEntity t = boss.getTarget();
        phaseTicks++;
        switch (boss.getBossState()) {
            case CANNON_SHOT -> {
                if (phaseTicks == boss.scaledTicks(CANNON_FIRE_TICK) && t != null) fireCannon(t);
                if (phaseTicks >= boss.scaledTicks(BossState.CANNON_SHOT.getBaseTicks())) finished = true;
            }
            case BEAM_START -> {
                track(t);
                if (phaseTicks % 2 == 0) beam(false);          // telegraph: solo partículas
                if (phaseTicks >= boss.scaledTicks(BossState.BEAM_START.getBaseTicks())) {
                    enter(BossState.BEAM);
                    boss.playSound(SoundEvents.BEACON_ACTIVATE, 3.0F, 1.4F);
                }
            }
            case BEAM -> {
                track(t);
                beam(phaseTicks % BEAM_HIT_INTERVAL == 0);
                if (phaseTicks >= boss.scaledTicks(BossState.BEAM.getBaseTicks())) {
                    enter(BossState.BEAM_END);
                    boss.playSound(SoundEvents.BEACON_DEACTIVATE, 3.0F, 1.4F);
                }
            }
            case BEAM_END -> {
                if (phaseTicks >= boss.scaledTicks(BossState.BEAM_END.getBaseTicks())) finished = true;
            }
            default -> finished = true;
        }
    }

    // ---- Cannon: bola de fuego tipo Ghast, marcada para detectar cuando vuelve
    private void fireCannon(LivingEntity target) {
        Vec3 origin = boss.getEyePosition();                       // ajusta a la altura del cañón si hace falta
        Vec3 dir = target.getEyePosition().subtract(origin).normalize();
        LargeFireball fb = new LargeFireball(boss.level(), boss, dir.x, dir.y, dir.z, 1);
        fb.setPos(origin.x + dir.x * 2.0D, origin.y + dir.y * 2.0D, origin.z + dir.z * 2.0D);
        fb.getPersistentData().putBoolean(ForgottenConstructEntity.FIREBALL_TAG, true);
        boss.level().addFreshEntity(fb);
        boss.playSound(SoundEvents.GHAST_SHOOT, 3.0F, 0.6F);
    }

    // ---- Beam
    private void track(LivingEntity t) {
        if (t == null) return;
        Vec3 desired = t.getEyePosition().subtract(boss.getEyePosition()).normalize();
        aim = aim.add(desired.subtract(aim).scale(BEAM_TRACKING)).normalize();
    }

    private void beam(boolean dealDamage) {
        if (!(boss.level() instanceof ServerLevel sl)) return;
        Vec3 start = boss.getEyePosition();
        Vec3 end = start.add(aim.scale(BEAM_RANGE));
        BlockHitResult hit = sl.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        if (hit.getType() != HitResult.Type.MISS) end = hit.getLocation();

        // Partículas a lo largo del rayo
        double len = start.distanceTo(end);
        for (double d = 1.0D; d < len; d += 0.8D) {
            Vec3 p = start.add(aim.scale(d));
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        if (!dealDamage) return;

        AABB box = new AABB(start, end).inflate(1.0D);
        for (Player p : sl.getEntitiesOfClass(Player.class, box, pl -> pl.isAlive() && !pl.isCreative() && !pl.isSpectator())) {
            Optional<Vec3> res = p.getBoundingBox().inflate(0.3D).clip(start, end);
            if (res.isEmpty()) continue;
            if (p.isBlocking()) disableShield(p);
            p.hurt(sl.damageSources().indirectMagic(boss, boss), BEAM_DAMAGE);   // daño mágico
        }
    }

    /** Deshabilita el escudo de forma garantizada (Player#disableShield es probabilístico). */
    private void disableShield(Player p) {
        ItemStack shield = p.getUseItem();
        p.getCooldowns().addCooldown(shield.getItem(), SHIELD_DISABLE_TICKS);
        p.stopUsingItem();
        p.level().broadcastEntityEvent(p, (byte) 30);   // sonido de escudo roto
    }

    @Override
    public void stop() {
        returnToIdle();
        boss.setRangedCooldown(boss.scaledTicks(COOLDOWN + boss.getRandom().nextInt(30)));
    }
}

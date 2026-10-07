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
import net.minecraft.util.Mth;
import net.tucas.sculkeritegreatsword.entity.custom.BossState;
import net.tucas.sculkeritegreatsword.entity.custom.ForgottenConstructEntity;

public class BossRangedAttackGoal extends AbstractBossGoal {
    private static final int CANNON_FIRE_TICK = 21;
    private static final double BEAM_RANGE = 20.0D;
    private static final float BEAM_DAMAGE = 5.0F;
    private static final int BEAM_HIT_INTERVAL = 10;
    private static final double BEAM_TRACKING = 0.6D;
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
        Vec3 from = boss.locatorPos(ForgottenConstructEntity.NUCLEO_OFFSET);
        aim = t != null ? t.getEyePosition().subtract(from).normalize() : boss.getLookAngle();
        enter(boss.getRandom().nextBoolean() ? BossState.BEAM_START : BossState.CANNON_SHOT);
    }

    private void enter(BossState s) { boss.setBossState(s); phaseTicks = 0; }

    @Override
    public void tick() {
        LivingEntity t = boss.getTarget();
        phaseTicks++;
        switch (boss.getBossState()) {
            case CANNON_SHOT -> {
                int fire = boss.scaledTicks(CANNON_FIRE_TICK);
                if (phaseTicks >= fire - 6 && phaseTicks < fire) cannonCharge();                    // NUEVO: carga visible
                if (phaseTicks == fire && t != null) fireCannon(t);
                if (phaseTicks >= boss.scaledTicks(BossState.CANNON_SHOT.getBaseTicks())) finished = true;
            }
            case BEAM_START -> {
                track(t);
                telegraph();
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
                    boss.clearBeam();
                    boss.playSound(SoundEvents.BEACON_DEACTIVATE, 3.0F, 1.4F);
                }
            }
            case BEAM_END -> {
                if (phaseTicks >= boss.scaledTicks(BossState.BEAM_END.getBaseTicks())) finished = true;
            }
            default -> finished = true;
        }
    }

    // ---- Cannon: sale del locator "cannon"

    /** NUEVO: partículas y sonido en la boca del cañón mientras carga. */
    private void cannonCharge() {
        if (!(boss.level() instanceof ServerLevel sl)) return;
        Vec3 p = boss.locatorPos(ForgottenConstructEntity.CANNON_OFFSET);
        sl.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 3, 0.25, 0.25, 0.25, 0.02);
        if (phaseTicks % 5 == 0) boss.playSound(SoundEvents.GHAST_WARN, 2.0F, 1.0F);
    }

    private void fireCannon(LivingEntity target) {
        Vec3 origin = boss.locatorPos(ForgottenConstructEntity.CANNON_OFFSET);
        Vec3 dir = target.getEyePosition().subtract(origin).normalize();
        LargeFireball fb = new LargeFireball(boss.level(), boss, dir.x, dir.y, dir.z, 1);
        fb.setPos(origin.x, origin.y, origin.z);                          // CAMBIO: sin desplazamiento
        fb.getPersistentData().putBoolean(ForgottenConstructEntity.FIREBALL_TAG, true);
        boss.level().addFreshEntity(fb);

        // NUEVO: efecto visible al disparar
        if (boss.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LAVA, origin.x, origin.y, origin.z, 12, 0.3, 0.3, 0.3, 0.1);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, origin.x, origin.y, origin.z, 8, 0.3, 0.3, 0.3, 0.05);
        }
        boss.playSound(SoundEvents.GHAST_SHOOT, 3.0F, 0.6F);
    }

    // ---- Beam: sale del locator "nucleo"
    private void track(LivingEntity t) {
        if (t == null) return;
        Vec3 from = boss.locatorPos(ForgottenConstructEntity.NUCLEO_OFFSET);
        Vec3 desired = t.getEyePosition().subtract(from).normalize();
        aim = aim.add(desired.subtract(aim).scale(BEAM_TRACKING)).normalize();
    }

    /** Carga previa: partículas que se concentran en el pecho. */
    private void telegraph() {
        if (!(boss.level() instanceof ServerLevel sl)) return;
        Vec3 p = boss.locatorPos(ForgottenConstructEntity.NUCLEO_OFFSET);
        sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.4, 0.4, 0.4, 0.02);
    }

    private void beam(boolean dealDamage) {
        if (!(boss.level() instanceof ServerLevel sl)) return;
        Vec3 start = boss.locatorPos(ForgottenConstructEntity.NUCLEO_OFFSET);
        Vec3 end = start.add(aim.scale(BEAM_RANGE));
        BlockHitResult hit = sl.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        if (hit.getType() != HitResult.Type.MISS) end = hit.getLocation();

        float yaw = (float) (Mth.atan2(-aim.x, aim.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (-Math.asin(Mth.clamp(aim.y, -1.0D, 1.0D)) * Mth.RAD_TO_DEG);
        boss.setBeam(yaw, pitch, (float) start.distanceTo(end));

        if (!dealDamage) return;
        AABB box = new AABB(start, end).inflate(1.0D);
        for (Player p : sl.getEntitiesOfClass(Player.class, box, pl -> pl.isAlive() && !pl.isCreative() && !pl.isSpectator())) {
            if (p.getBoundingBox().inflate(0.3D).clip(start, end).isEmpty()) continue;
            if (p.isBlocking()) disableShield(p);
            p.hurt(sl.damageSources().indirectMagic(boss, boss), BEAM_DAMAGE);
        }
    }

    private void disableShield(Player p) {
        ItemStack shield = p.getUseItem();
        p.getCooldowns().addCooldown(shield.getItem(), SHIELD_DISABLE_TICKS);
        p.stopUsingItem();
        p.level().broadcastEntityEvent(p, (byte) 30);
    }

    @Override
    public void stop() {
        boss.clearBeam();
        returnToIdle();
        boss.setRangedCooldown(boss.scaledTicks(COOLDOWN + boss.getRandom().nextInt(30)));
    }
}
package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.tucas.sculkeritegreatsword.init.ModEntities;
import net.tucas.sculkeritegreatsword.item.Moditems;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SporesShroomersEntity extends ThrowableItemProjectile {

    private static final DustParticleOptions LILAC_DUST =
            new DustParticleOptions(new Vector3f(0.78f, 0.64f, 0.98f), 1.3F);

    private static final int DETONATION_DURATION = 160;

    private boolean detonated = false;
    private int detonationTicks = 0;
    private LivingEntity struckTarget;
    private List<Integer> spawnSchedule;
    private int nextSpawnIndex = 0;
    private int totalServants;

    public SporesShroomersEntity(EntityType<? extends SporesShroomersEntity> type, Level level) {
        super(type, level);
    }

    public SporesShroomersEntity(Level level, LivingEntity owner) {
        super(ModEntities.SPORES_SHROOMERS.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Moditems.SPORES_SHROOMERS.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide && !this.detonated) {
            detonate();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity livingEntity
                && livingEntity != this.getOwner()) {
            this.struckTarget = livingEntity;
        }
    }

    private void detonate() {
        this.detonated = true;
        this.detonationTicks = 0;

        // DESAPARECER EL ITEM COMPLETAMENTE
        this.setInvisible(true);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        this.totalServants = 5;
        if (this.random.nextFloat() < 0.15F) {
            this.totalServants = 5 + this.random.nextInt(4);
        }

        this.spawnSchedule = new ArrayList<>();
        for (int i = 0; i < this.totalServants; i++) {
            int slot = (int) ((DETONATION_DURATION * 0.8) * (i + 1) / (double) (this.totalServants + 1));
            int jitter = this.random.nextInt(9) - 4;
            this.spawnSchedule.add(Math.max(0, slot + jitter));
        }
        Collections.sort(this.spawnSchedule);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.detonated) {
            return;
        }

        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            spawnPurpleSmoke(serverLevel);

            while (nextSpawnIndex < spawnSchedule.size() && detonationTicks >= spawnSchedule.get(nextSpawnIndex)) {
                spawnSingleServant(serverLevel);
                nextSpawnIndex++;
            }

            detonationTicks++;

            if (detonationTicks >= DETONATION_DURATION) {
                this.discard();
            }
        }
    }

    private void spawnPurpleSmoke(ServerLevel serverLevel) {
        float progress = 1.0F - ((float) detonationTicks / DETONATION_DURATION);
        int particlesThisTick = Math.round(4 * progress);
        if (particlesThisTick <= 0 && this.random.nextFloat() > progress) {
            return;
        }

        for (int i = 0; i < Math.max(particlesThisTick, 1); i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 2.2;
            double offsetY = this.random.nextDouble() * 0.2;
            double offsetZ = (this.random.nextDouble() - 0.5) * 2.2;

            serverLevel.sendParticles(
                    LILAC_DUST,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                    3, 0.15, 0.0, 0.15, 0.0
            );
        }
    }

    private void spawnSingleServant(ServerLevel serverLevel) {
        Mob servant = ModEntities.MUSHROOM_SERVANT.get().create(serverLevel);
        if (servant == null) return;

        Player owner = (this.getOwner() instanceof Player player) ? player : null;

        double spreadX = (this.random.nextDouble() - 0.5) * 1.5;
        double spreadZ = (this.random.nextDouble() - 0.5) * 1.5;

        net.minecraft.world.phys.Vec3 safePos = findSafeSpawnPos(serverLevel, servant,
                this.getX() + spreadX, this.getY(), this.getZ() + spreadZ);

        servant.moveTo(safePos.x, safePos.y, safePos.z,
                this.random.nextFloat() * 360.0F, 0.0F);

        servant.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null, null);

        if (servant instanceof net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity mushroomServant
                && owner != null) {
            mushroomServant.tame(owner);
            mushroomServant.setPersistenceRequired();
        }

        if (this.struckTarget != null && this.struckTarget.isAlive()) {
            servant.setTarget(this.struckTarget);
        }

        serverLevel.addFreshEntity(servant);
    }

    private net.minecraft.world.phys.Vec3 findSafeSpawnPos(ServerLevel serverLevel, Mob servant,
                                                           double candidateX, double candidateY, double candidateZ) {
        double[][] attempts = {
                {candidateX, candidateY, candidateZ},
                {candidateX, candidateY + 1.0, candidateZ},
                {this.getX(), this.getY(), this.getZ()},
                {this.getX(), this.getY() + 1.0, this.getZ()}
        };

        for (double[] pos : attempts) {
            net.minecraft.world.phys.AABB testBox = servant.getType().getDimensions()
                    .makeBoundingBox(pos[0], pos[1], pos[2]);
            if (serverLevel.noCollision(testBox)) {
                return new net.minecraft.world.phys.Vec3(pos[0], pos[1], pos[2]);
            }
        }

        return new net.minecraft.world.phys.Vec3(this.getX(), this.getY(), this.getZ());
    }
}
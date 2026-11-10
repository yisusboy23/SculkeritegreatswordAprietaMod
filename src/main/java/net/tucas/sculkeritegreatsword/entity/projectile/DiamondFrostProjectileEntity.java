package net.tucas.sculkeritegreatsword.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class DiamondFrostProjectileEntity extends ThrowableItemProjectile {

    public DiamondFrostProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public DiamondFrostProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL; // cambia por tu ítem custom si tienes uno
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity shooter = this.getOwner();

        // Resolver el verdadero dueño (jugador dueño del golem)
        Entity trueOwner = null;
        if (shooter instanceof TamableAnimal tamable && tamable.isTame()) {
            trueOwner = tamable.getOwner();
        }

        // Ignorar al golem que disparó
        if (target == shooter) return;

        // Ignorar al dueño del golem
        if (trueOwner != null && target == trueOwner) return;

        // Ignorar mascotas del mismo dueño
        if (trueOwner != null && target instanceof TamableAnimal tamable
                && tamable.isTame() && tamable.getOwner() == trueOwner) return;

        // Ignorar cualquier entidad OwnableEntity del mismo dueño
        if (trueOwner != null && target instanceof OwnableEntity ownable
                && ownable.getOwner() == trueOwner) return;

        // Aplicar daño y efectos
        if (target instanceof LivingEntity living) {
            DamageSource damageSource = this.damageSources().thrown(this, shooter);
            living.hurt(damageSource, 12.0f);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0));
        }

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        // super.onHit ya llamó a onHitEntity u onHitBlock según corresponda.
        // Aquí solo hacemos efectos visuales/sonoros genéricos.
        // NOTA: si no quieres doble partícula al impactar entidad,
        // mueve esto a onHitBlock() en lugar de onHit().
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            ServerLevel level = (ServerLevel) this.level();
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.1);
            this.playSound(SoundEvents.GLASS_BREAK, 1.0F, 1.0F);
            this.discard();
        }
    }
}
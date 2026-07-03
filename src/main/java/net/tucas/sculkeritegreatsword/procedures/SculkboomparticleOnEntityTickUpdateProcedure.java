package net.tucas.sculkeritegreatsword.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.tucas.sculkeritegreatsword.Sculkeritegreatsword;

public class SculkboomparticleOnEntityTickUpdateProcedure {
    public static void execute(LevelAccessor world, Entity entity) {
        if (entity == null) return;

        // Ignora gravedad
        entity.setNoGravity(true);

        // Aplica impulso visual con partículas (simula propulsión sculk)
        entity.makeStuckInBlock(Blocks.SCULK.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));

        // Desaparece luego de 1 segundo (20 ticks)
        Sculkeritegreatsword.queueServerWork(20, () -> {
            if (!entity.level().isClientSide()) {
                entity.discard();
            }
        });
    }
}
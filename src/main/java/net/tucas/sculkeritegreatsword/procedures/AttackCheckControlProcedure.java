package net.tucas.sculkeritegreatsword.procedures;
import net.minecraft.world.entity.Entity;

public class AttackCheckControlProcedure {
    public static boolean execute(Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getPersistentData().getBoolean("attack_hostiles")
                && !entity.getPersistentData().getBoolean("defense");
    }
}
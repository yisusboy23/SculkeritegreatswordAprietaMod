package net.tucas.sculkeritegreatsword.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.tucas.sculkeritegreatsword.entity.custom.MushroomServantEntity;
import net.tucas.sculkeritegreatsword.init.ModEntities;

public class MushroomServantSpawnEggItem extends Item {

    public MushroomServantSpawnEggItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = pos.relative(direction);
        ItemStack itemStack = context.getItemInHand();

        // Spawnear la entidad usando ModEntities directamente
        EntityType<?> entityType = ModEntities.MUSHROOM_SERVANT.get();
        Mob entity = (Mob) entityType.spawn(serverLevel, itemStack.getTag(),
                null, spawnPos, MobSpawnType.SPAWN_EGG,
                true, !pos.equals(spawnPos) && direction == Direction.UP);

        if (entity instanceof MushroomServantEntity servant && player != null) {
            // DOMESTICAR automáticamente al jugador que lo spawneó
            servant.tame(player);
            servant.setPersistenceRequired();
        }

        if (entity != null) {
            itemStack.shrink(1);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        }

        return InteractionResult.CONSUME;
    }
}
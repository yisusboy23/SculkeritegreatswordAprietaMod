package net.tucas.sculkeritegreatsword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.WaterAnimal;

import java.util.List;

public class TersectactItem extends Item {
    private static final int MAX_ENTITIES = 3;
    private static final int USE_DURATION = 72000;

    public TersectactItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide && entity instanceof Player player) {
            int count = getStoredCount(stack);
            CompoundTag tag = stack.getOrCreateTag();

            // ¿Está el item en la mano del jugador?
            boolean inMainHand = player.getMainHandItem() == stack;
            boolean inOffHand = player.getOffhandItem() == stack;
            boolean inHand = inMainHand || inOffHand;

            // custom_model_data = 0 para inventario, 1-4 para mano
            int modelData = inHand ? (count + 1) : 0;

            if (tag.getInt("CustomModelData") != modelData) {
                tag.putInt("CustomModelData", modelData);
                stack.setTag(tag);
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            // Actualizar modelo para vista en mano inmediatamente
            CompoundTag tag = stack.getOrCreateTag();
            int count = getStoredCount(stack);
            tag.putInt("CustomModelData", count + 1);
            stack.setTag(tag);
            return InteractionResultHolder.consume(stack);
        }

        if (player.isCrouching()) {
            if (releaseEntities(stack, level, player)) {
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);

                player.displayClientMessage(
                        Component.literal("Criaturas liberadas").withStyle(ChatFormatting.AQUA),
                        true
                );

                // Actualizar modelo
                CompoundTag tag = stack.getOrCreateTag();
                tag.putInt("CustomModelData", 1); // En mano pero vacío
                stack.setTag(tag);

                return InteractionResultHolder.consume(stack);
            } else {
                player.displayClientMessage(
                        Component.literal("El Tersectact está vacío").withStyle(ChatFormatting.GRAY),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        int count = getStoredCount(stack);
        player.displayClientMessage(
                Component.literal("Criaturas: " + count + "/3")
                        .withStyle(ChatFormatting.YELLOW),
                true
        );

        // Actualizar modelo
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("CustomModelData", count + 1);
        stack.setTag(tag);

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    public static boolean storeEntity(ItemStack stack, LivingEntity entity, Player player) {
        CompoundTag stackTag = stack.getOrCreateTag();

        if (!stackTag.contains("StoredEntities")) {
            stackTag.put("StoredEntities", new ListTag());
        }

        ListTag entities = stackTag.getList("StoredEntities", Tag.TAG_COMPOUND);

        if (entities.size() >= MAX_ENTITIES) {
            player.displayClientMessage(
                    Component.literal("El Tersectact está lleno (3/3)").withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        CompoundTag entityData = new CompoundTag();
        entity.saveWithoutId(entityData);
        String encodeId = entity.getEncodeId();
        if (encodeId != null) {
            entityData.putString("id", encodeId);
        } else {
            return false;
        }

        entities.add(entityData);
        stackTag.put("StoredEntities", entities);

        // Actualizar modelo inmediatamente
        int newCount = entities.size();
        stackTag.putInt("CustomModelData", newCount + 1);

        stack.setTag(stackTag);

        return true;
    }

    private boolean releaseEntities(ItemStack stack, Level level, Player player) {
        CompoundTag stackTag = stack.getTag();

        if (stackTag == null || !stackTag.contains("StoredEntities")) {
            return false;
        }

        ListTag entities = stackTag.getList("StoredEntities", Tag.TAG_COMPOUND);

        if (entities.isEmpty()) {
            return false;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        int liberadas = 0;

        for (int i = 0; i < entities.size(); i++) {
            CompoundTag entityData = entities.getCompound(i);
            final int index = i;

            try {
                Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(
                        entityData,
                        serverLevel,
                        (loadedEntity) -> {
                            double angle = (Math.PI * 2 * index) / entities.size();
                            double distance = 3.0;

                            double x = player.getX() + Math.cos(angle) * distance;
                            double z = player.getZ() + Math.sin(angle) * distance;

                            BlockPos targetPos = BlockPos.containing(x, player.getY(), z);
                            double y = findSafeY(serverLevel, targetPos);

                            loadedEntity.moveTo(x, y, z, player.getYRot(), 0);
                            loadedEntity.setYHeadRot(player.getYRot());

                            return loadedEntity;
                        }
                );

                if (entity != null) {
                    boolean added = serverLevel.addFreshEntity(entity);

                    if (added) {
                        liberadas++;

                        if (entity instanceof TamableAnimal tamable) {
                            tamable.setOrderedToSit(true);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        stackTag.remove("StoredEntities");
        // Resetear modelo a vacío en mano
        stackTag.putInt("CustomModelData", 1);
        stack.setTag(stackTag);

        return liberadas > 0;
    }

    private double findSafeY(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 5; i++) {
            BlockPos check = pos.above(i);
            if (level.getBlockState(check).isAir() &&
                    level.getBlockState(check.above()).isAir() &&
                    !level.getBlockState(check.below()).isAir()) {
                return check.getY();
            }
        }
        return pos.getY() + 1;
    }

    public static int getStoredCount(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        if (stackTag == null || !stackTag.contains("StoredEntities")) {
            return 0;
        }
        return stackTag.getList("StoredEntities", Tag.TAG_COMPOUND).size();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int count = getStoredCount(stack);

        if (count > 0) {
            tooltip.add(Component.literal("Criaturas: " + count + "/3")
                    .withStyle(ChatFormatting.AQUA));

            CompoundTag stackTag = stack.getTag();
            if (stackTag != null) {
                ListTag entities = stackTag.getList("StoredEntities", Tag.TAG_COMPOUND);

                for (int i = 0; i < entities.size(); i++) {
                    String id = entities.getCompound(i).getString("id");
                    String name = id.contains(":") ? id.split(":")[1].replace("_", " ") : id;
                    tooltip.add(Component.literal("  • " + name)
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        } else {
            tooltip.add(Component.literal("Vacío (0/3)")
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Click derecho en criatura: Capturar")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Shift + Click derecho: Liberar")
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getStoredCount(stack) > 0;
    }
    @Override
    public @NotNull InteractionResult interactLivingEntity(
            @NotNull ItemStack stack,
            @NotNull Player player,
            @NotNull LivingEntity target,
            @NotNull InteractionHand hand) {

        return InteractionResult.PASS;
    }
}
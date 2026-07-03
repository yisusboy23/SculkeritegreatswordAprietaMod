package net.tucas.sculkeritegreatsword.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.tucas.sculkeritegreatsword.entity.custom.DrillerEntity;
import net.tucas.sculkeritegreatsword.item.Moditems;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class DrillerDigGoal extends Goal {

    private final DrillerEntity driller;
    private int digTimer = 0;
    private boolean arrived = false;
    private BlockPos targetOre = null;
    private boolean didDrop = false;
    private int globalTimeout = 0;

    private static final List<Block> VALID_ORES = List.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE
    );

    private static final Map<Block, Item> ORE_DROPS = Map.ofEntries(
            Map.entry(Blocks.COAL_ORE, Items.COAL),
            Map.entry(Blocks.DEEPSLATE_COAL_ORE, Items.COAL),
            Map.entry(Blocks.IRON_ORE, Items.RAW_IRON),
            Map.entry(Blocks.DEEPSLATE_IRON_ORE, Items.RAW_IRON),
            Map.entry(Blocks.COPPER_ORE, Items.RAW_COPPER),
            Map.entry(Blocks.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER),
            Map.entry(Blocks.GOLD_ORE, Items.RAW_GOLD),
            Map.entry(Blocks.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD),
            Map.entry(Blocks.REDSTONE_ORE, Items.REDSTONE),
            Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE),
            Map.entry(Blocks.EMERALD_ORE, Items.EMERALD),
            Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, Items.EMERALD),
            Map.entry(Blocks.LAPIS_ORE, Items.LAPIS_LAZULI),
            Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI),
            Map.entry(Blocks.DIAMOND_ORE, Items.DIAMOND),
            Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND)
    );

    public DrillerDigGoal(DrillerEntity driller) {
        this.driller = driller;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (driller.isBaby()) return false;

        return !driller.level().isClientSide && driller.isDigging();
    }

    @Override
    public boolean canContinueToUse() {
        return !driller.level().isClientSide && driller.isDigging();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        digTimer = 60;
        arrived = false;
        didDrop = false;
        globalTimeout = 200;
        targetOre = findNearestOre();  // CAMBIADO: ahora busca el más cercano
        if (targetOre != null) {
            driller.getNavigation().moveTo(
                    targetOre.getX() + 0.5,
                    targetOre.getY() + 0.5,
                    targetOre.getZ() + 0.5,
                    1.0
            );
        } else {
            driller.setDigging(false);
        }
    }

    @Override
    public void tick() {
        if (driller.level().isClientSide) return;

        globalTimeout--;
        if (globalTimeout <= 0) {
            driller.setDigging(false);
            return;
        }

        if (targetOre == null) {
            driller.setDigging(false);
            return;
        }

        driller.getLookControl().setLookAt(
                targetOre.getX() + 0.5,
                targetOre.getY() + 0.5,
                targetOre.getZ() + 0.5,
                30f, 30f
        );

        if (!arrived) {
            double dist = driller.distanceToSqr(
                    targetOre.getX() + 0.5,
                    targetOre.getY() + 0.5,
                    targetOre.getZ() + 0.5
            );
            if (dist <= 2.25) {
                arrived = true;
                driller.getNavigation().stop();
            }
            return;
        }

        driller.getNavigation().stop();
        digTimer--;

        if (digTimer <= 0 && !didDrop) {
            didDrop = true;
            dropOre();
            driller.setDigging(false);
        }
    }

    @Override
    public void stop() {
        digTimer = 0;
        arrived = false;
        targetOre = null;
        didDrop = false;
        globalTimeout = 0;
        driller.getNavigation().stop();
    }

    private void dropOre() {
        if (targetOre == null) return;

        Item drop;

        // 10% de probabilidad de soltar Driller Claw
        if (driller.getRandom().nextFloat() < 0.10F) {
            drop = Moditems.DRILLER_CLAW.get();
        } else {
            Block block = driller.level().getBlockState(targetOre).getBlock();
            drop = ORE_DROPS.get(block);
        }

        if (drop == null) return;

        int amount = 2 + driller.getRandom().nextInt(3);

        driller.level().addFreshEntity(
                new ItemEntity(
                        driller.level(),
                        targetOre.getX() + 0.5,
                        targetOre.getY() + 0.5,
                        targetOre.getZ() + 0.5,
                        new ItemStack(drop, amount)
                )
        );
    }
    // CORREGIDO: ahora encuentra el mineral MÁS CERCANO, no uno al azar
    private BlockPos findNearestOre() {
        BlockPos myPos = driller.blockPosition();
        BlockPos nearestOre = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos checkPos = new BlockPos(myPos.getX() + x, myPos.getY() + y, myPos.getZ() + z);
                    Block block = driller.level().getBlockState(checkPos).getBlock();
                    if (VALID_ORES.contains(block)) {
                        double distance = Math.sqrt(x * x + y * y + z * z);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestOre = checkPos.immutable();
                        }
                    }
                }
            }
        }
        return nearestOre;
    }
}
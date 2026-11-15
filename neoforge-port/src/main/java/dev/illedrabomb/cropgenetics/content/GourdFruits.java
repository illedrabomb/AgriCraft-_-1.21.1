package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Physical fruit placement, deliberately restricted to the two supported
 * vanilla gourds. A fruit uses our owner-aware block and Minecraft's visual
 * and loot behavior. Both sides store a UUID to prevent unrelated matches.
 */
public final class GourdFruits {
    private GourdFruits() {}

    @Nullable
    public static Block fruitBlock(PlantDefinition plant) {
        if (plant.harvestMode() != HarvestMode.GOURD) return null;
        return switch (plant.id()) {
            case "minecraft:melon_plant" -> CropBlocks.GENETIC_MELON_FRUIT.get();
            case "minecraft:pumpkin_plant" -> CropBlocks.GENETIC_PUMPKIN_FRUIT.get();
            default -> null; // Other gourds require an explicit compatibility adapter.
        };
    }

    public static boolean matches(ServerLevel level, BlockPos stem,
                                  Direction direction, PlantDefinition plant, UUID token) {
        Block block = fruitBlock(plant);
        if (block == null || direction.getAxis().isVertical()) return false;
        BlockPos fruitPos = stem.relative(direction);
        return level.getBlockState(fruitPos).is(block)
                && level.getBlockEntity(fruitPos) instanceof GeneticGourdFruitBlockEntity fruit
                && fruit.ownedBy(stem, token);
    }

    /** Places a real fruit only in empty space above an ordinary growable soil. */
    public static boolean tryPlace(ServerLevel level, BlockPos stem, PlantDefinition plant,
                                   CropSticksBlockEntity crop, RandomSource random) {
        Block fruit = fruitBlock(plant);
        if (fruit == null || crop.fruitReady()) return false;
        List<Direction> candidates = new ArrayList<>(List.of(
                Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        for (Direction direction : candidates) {
            BlockPos target = stem.relative(direction);
            if (!level.isEmptyBlock(target)) continue;
            BlockState ground = level.getBlockState(target.below());
            if (!(ground.is(Blocks.FARMLAND) || ground.is(Blocks.DIRT)
                    || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.COARSE_DIRT)
                    || ground.is(Blocks.PODZOL))) continue;

            UUID token = UUID.randomUUID();
            if (level.setBlock(target, fruit.defaultBlockState(), 3)) {
                if (level.getBlockEntity(target) instanceof GeneticGourdFruitBlockEntity fruitEntity) {
                    fruitEntity.bind(stem, token);
                    if (crop.markPhysicalFruit(plant, direction, token)) return true;
                }
                // On failed binding discard the new block without fruit drops.
                level.removeBlock(target, false);
            }
        }
        return false;
    }
}

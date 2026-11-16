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
        return fruitBlockForSpecies(plant.id());
    }

    /**
     * Previously placed fruits keep their identity when /reload changes the
     * species' harvest mode. Only placement requires GOURD mode.
     */
    @Nullable
    private static Block fruitBlockForSpecies(String speciesId) {
        return switch (speciesId) {
            case "minecraft:melon_plant" -> CropBlocks.GENETIC_MELON_FRUIT.get();
            case "minecraft:pumpkin_plant" -> CropBlocks.GENETIC_PUMPKIN_FRUIT.get();
            default -> null; // Other gourds require an explicit compatibility adapter.
        };
    }

    public static boolean matches(ServerLevel level, BlockPos stem,
                                  Direction direction, PlantDefinition plant, UUID token) {
        return matchesOwned(level, stem, direction, plant.id(), token);
    }

    /** Match an existing owned fruit regardless of the active harvest mode. */
    public static boolean matchesOwned(ServerLevel level, BlockPos stem,
                                       Direction direction, String speciesId, UUID token) {
        Block block = fruitBlockForSpecies(speciesId);
        if (block == null || direction.getAxis().isVertical()) return false;
        BlockPos fruitPos = stem.relative(direction);
        return level.hasChunkAt(fruitPos) && level.getBlockState(fruitPos).is(block)
                && level.getBlockEntity(fruitPos) instanceof GeneticGourdFruitBlockEntity fruit
                && fruit.ownedBy(stem, token);
    }

    /** Places a real fruit only in empty space above an ordinary growable soil. */
    public static boolean tryPlace(ServerLevel level, BlockPos stem, PlantDefinition plant,
                                   CropSticksBlockEntity crop, RandomSource random) {
        Block fruit = fruitBlock(plant);
        // Placement happens on the server thread. Recheck the exact stem
        // before touching an adjacent block: block callbacks can replace it.
        if (fruit == null || crop.isRemoved() || crop.fruitReady()
                || level.getBlockEntity(stem) != crop
                || !level.getBlockState(stem).is(CropBlocks.CROP_STICKS.get())
                || !crop.isMature(plant)) return false;
        List<Direction> candidates = new ArrayList<>(List.of(
                Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        for (Direction direction : candidates) {
            BlockPos target = stem.relative(direction);
            if (!level.hasChunkAt(target) || !level.isEmptyBlock(target)) continue;
            BlockState ground = level.getBlockState(target.below());
            if (!(ground.is(Blocks.FARMLAND) || ground.is(Blocks.DIRT)
                    || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.COARSE_DIRT)
                    || ground.is(Blocks.PODZOL))) continue;

            UUID token = UUID.randomUUID();
            if (level.setBlock(target, fruit.defaultBlockState(), 3)) {
                // Save the entity observed immediately after placement. On a
                // failed bind only roll back if the same block/entity remains;
                // never delete an unrelated block installed by a callback.
                var placedEntity = level.getBlockEntity(target);
                if (placedEntity instanceof GeneticGourdFruitBlockEntity fruitEntity
                        && fruitEntity.bind(stem, token)
                        && crop.markPhysicalFruit(plant, direction, token)) return true;

                if (level.getBlockState(target).is(fruit)
                        && level.getBlockEntity(target) == placedEntity) {
                    level.removeBlock(target, false);
                }
            }
        }
        return false;
    }
}

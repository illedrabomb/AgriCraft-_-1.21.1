package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.MutationDefinition;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.genetics.BreedingEngine;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Server-side genetic offspring selection for an unoccupied double-crop. */
public final class CrossBreeding {
    private CrossBreeding() {}

    public static void attempt(ServerLevel level, BlockPos position, CropSticksBlockEntity target,
                               RandomSource random) {
        if (target.genome() != null || !target.getBlockState().getValue(CropSticksBlock.CROSS)) return;
        AgriPlantsRegistry registry = CropGeneticsMod.plants();
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        Map<String, PlantDefinition> plants = snapshot.plants();
        List<Genome> parents = new ArrayList<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = position.relative(direction);
            // Never force chunk loads during ordinary crop random ticks.
            if (!level.hasChunkAt(neighbor)) continue;
            if (!(level.getBlockEntity(neighbor) instanceof CropSticksBlockEntity source)
                    || source.getBlockState().getValue(CropSticksBlock.CROSS)) continue;
            Genome genome = source.genome();
            if (genome == null) continue;
            PlantDefinition plant = plants.get(genome.plantId());
            if (plant == null || !source.isMature(plant)
                    || !CropConditions.canGrow(level, neighbor, plant, genome)) continue;

            // Fertility is retained as a hidden statistic and affects breeding.
            double fertility = genome.fertility().expressed(GenePair.TraitMode.MAX) / 10.0D;
            if (random.nextDouble() < fertility) parents.add(genome);
        }

        if (parents.isEmpty()) return;
        Random inheritanceRandom = new Random(random.nextLong());
        Collections.shuffle(parents, inheritanceRandom);

        if (parents.size() == 1) {
            Genome parent = parents.getFirst();
            PlantDefinition plant = plants.get(parent.plantId());
            if (plant != null && plant.cloneable()
                    && CropConditions.canGrow(level, position, plant, parent)
                    && random.nextDouble() < plant.spreadChance()) {
                // Clone preserves the complete genotype; cloning is not recombination.
                target.acceptCrossOffspring(parent, plant);
            }
            return;
        }

        Genome first = parents.get(0);
        Genome second = parents.get(1);
        // Numeric alleles are inherited exactly once. Species choice must use
        // the offspring's actual Strength, not either parent's Strength.
        Genome inherited = BreedingEngine.combine(first, second, first.plantId(), inheritanceRandom);
        String childId = chooseChildSpecies(first, second, inherited, snapshot, level, position, random);
        PlantDefinition childPlant = plants.get(childId);
        if (childPlant == null) return;

        Genome offspring = inherited.withPlantId(childId);
        if (!CropConditions.canGrow(level, position, childPlant, offspring)) return;
        target.acceptCrossOffspring(offspring, childPlant);
    }

    private static String chooseChildSpecies(Genome first, Genome second, Genome inherited,
                                             AgriPlantsRegistry.Snapshot snapshot,
                                             ServerLevel level, BlockPos target, RandomSource random) {
        // Sort by datapack ID for deterministic iteration regardless of Map ordering.
        List<Map.Entry<String, MutationDefinition>> candidates = snapshot.mutations().entrySet()
                .stream()
                .filter(entry -> entry.getValue().matchesParents(first.plantId(), second.plantId()))
                .filter(entry -> CropConditions.matchesLocation(level, target,
                        entry.getValue().allowedBiomes(), entry.getValue().blockedBiomes(),
                        entry.getValue().allowedDimensions(), entry.getValue().blockedDimensions()))
                .filter(entry -> CropConditions.matchesEnvironment(level, target,
                        entry.getValue().environment()))
                .filter(entry -> CropConditions.matchesSeason(level, target,
                        entry.getValue().seasonRule().allowedSeasons()))
                .filter(entry -> snapshot.plants().containsKey(entry.getValue().childPlantId()))
                // Do not consume a mutation roll for species that cannot
                // grow under the actual soil and lighting with inherited Strength.
                .filter(entry -> CropConditions.canGrow(level, target,
                        snapshot.plants().get(entry.getValue().childPlantId()), inherited))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .toList();

        for (Map.Entry<String, MutationDefinition> entry : candidates) {
            if (random.nextDouble() < entry.getValue().chance()) {
                return entry.getValue().childPlantId();
            }
        }
        // With no viable mutation, prefer a parent species that the actual
        // inherited genome can grow at this destination (soil and lighting).
        String preferred = random.nextBoolean() ? first.plantId() : second.plantId();
        String alternate = preferred.equals(first.plantId()) ? second.plantId() : first.plantId();
        PlantDefinition preferredPlant = snapshot.plants().get(preferred);
        if (preferredPlant != null
                && CropConditions.canGrow(level, target, preferredPlant, inherited))
            return preferred;
        PlantDefinition otherPlant = snapshot.plants().get(alternate);
        if (otherPlant != null
                && CropConditions.canGrow(level, target, otherPlant, inherited))
            return alternate;
        return preferred; // caller still rejects unsuitable offspring safely
    }
}

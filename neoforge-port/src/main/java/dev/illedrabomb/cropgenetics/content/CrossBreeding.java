package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.config.CropBalanceConfig;
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
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Server-side genetic offspring selection for an unoccupied double-crop. */
public final class CrossBreeding {
    private CrossBreeding() {}

    private record BreedingParent(Direction direction, Genome genome) {}

    public static void attempt(ServerLevel level, BlockPos position, CropSticksBlockEntity target,
                               RandomSource random) {
        if (target.isRemoved() || level.getBlockEntity(position) != target) return;
        BlockState targetState = level.getBlockState(position);
        if (!targetState.is(CropBlocks.CROP_STICKS.get())
                || targetState.getValue(CropSticksBlock.PLANTED)
                || !targetState.getValue(CropSticksBlock.CROSS)
                || target.genome() != null) return;
        AgriPlantsRegistry registry = CropGeneticsMod.plants();
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        Map<String, PlantDefinition> plants = snapshot.plants();
        List<BreedingParent> parents = new ArrayList<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Genome genome = eligibleParent(level, position.relative(direction), plants);
            if (genome != null) parents.add(new BreedingParent(direction, genome));
        }

        // A straight, uninterrupted row is a separate, weaker breeding
        // arrangement: E E E S (three or more mature parents, one direction).
        // Direct two-, three- or four-sided crosses always take precedence.
        boolean straightRow = false;
        if (parents.size() == 1) {
            Direction rowDirection = parents.getFirst().direction();
            List<BreedingParent> row = new ArrayList<>(parents);
            for (int distance = 2; distance <= 16 && row.size() < 4; distance++) {
                Genome genome = eligibleParent(level, position.relative(rowDirection, distance), plants);
                if (genome == null) break;
                row.add(new BreedingParent(rowDirection, genome));
            }
            if (row.size() >= 3) {
                parents = row;
                straightRow = true;
            }
        }

        if (parents.isEmpty()) return;
        Random inheritanceRandom = new Random(random.nextLong());
        Collections.shuffle(parents, inheritanceRandom);

        if (parents.size() == 1) {
            Genome parent = parents.getFirst().genome();
            PlantDefinition plant = plants.get(parent.plantId());
            // One-parent spreading is a clone, not a genetic cross.
            double fertility = CrossFertilityRules.fertilityChance(parent);
            if (plant != null && plant.cloneable()
                    && CropConditions.canGrow(level, position, plant, parent)
                    && random.nextDouble() < fertility * plant.spreadChance()) {
                // Clone preserves the complete genotype; cloning is not recombination.
                target.acceptCrossOffspring(parent, plant);
            }
            return;
        }

        Genome first = parents.get(0).genome();
        Genome second = parents.get(1).genome();
        int parentCount = Math.min(4, parents.size());
        // Two parents are the weakest configuration (a straight row is one
        // example). Three and four eligible neighbors increase breeding
        // probability without requiring multiple independent parent rolls.
        double parentCountFactor = straightRow ? CrossFertilityRules.rowFertilityMultiplier()
                : CrossFertilityRules.parentCountMultiplier(parentCount);
        double chance = Math.min(1.0D, CrossFertilityRules.pairFertilityChance(first, second)
                * parentCountFactor * CropBalanceConfig.CROSS_FERTILITY_MULTIPLIER.get());
        if (random.nextDouble() >= chance) return;

        // The visible Growth/Gain/Strength values may come from ANY eligible
        // parent; 2, 3 and 4 parents respectively use 1x, 2x and 3x the
        // baseline mutation chance. Only one offspring is created.
        double mutationMultiplier = CropBalanceConfig.ROW_CROSS_EVOLUTION.get()
                ? (straightRow ? CrossFertilityRules.rowMutationMultiplier()
                        : CrossFertilityRules.mutationMultiplier(parentCount))
                        * CropBalanceConfig.ROW_EVOLUTION_MUTATION_MULTIPLIER.get()
                : 0.0D;
        Genome inherited = BreedingEngine.combine(
                parents.stream().map(BreedingParent::genome).toList(),
                first.plantId(), inheritanceRandom, mutationMultiplier);
        String childId = chooseChildSpecies(
                parents.stream().map(BreedingParent::genome).toList(),
                inherited, snapshot, level, position, random);
        PlantDefinition childPlant = plants.get(childId);
        if (childPlant == null) return;

        Genome offspring = inherited.withPlantId(childId);
        if (!CropConditions.canGrow(level, position, childPlant, offspring)) return;
        target.acceptCrossOffspring(offspring, childPlant);
    }

    private static String chooseChildSpecies(List<Genome> parentGenomes, Genome inherited,
                                             AgriPlantsRegistry.Snapshot snapshot,
                                             ServerLevel level, BlockPos target, RandomSource random) {
        // Sort by datapack ID for deterministic iteration regardless of Map ordering.
        List<Map.Entry<String, MutationDefinition>> candidates = snapshot.mutations().entrySet()
                .stream()
                .filter(entry -> CrossParentPairRules.matchesAnyPair(parentGenomes,
                        entry.getValue()::matchesParents))
                .filter(entry -> CropConditions.matchesLocation(level, target,
                        entry.getValue().allowedBiomes(), entry.getValue().blockedBiomes(),
                        entry.getValue().allowedDimensions(), entry.getValue().blockedDimensions()))
                .filter(entry -> CropConditions.matchesEnvironment(level, target,
                        entry.getValue().environment()))
                .filter(entry -> CropConditions.matchesSeason(level, target,
                        entry.getValue().seasonRule().allowedSeasons()))
                .filter(entry -> CropConditions.matchesChemistry(level, target,
                        entry.getValue().soilChemistry(),
                        inherited.strength()))
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
        // With no viable mutation, every participating parent species can
        // be inherited, not merely the first two in a 3-/4-parent cross.
        // Shuffle once for a fair choice; pick only a species that can grow
        // at the target under the inherited strength and environment.
        List<String> possibilities = new ArrayList<>();
        for (Genome parent : parentGenomes) possibilities.add(parent.plantId());
        Collections.shuffle(possibilities, new Random(random.nextLong()));
        for (String species : possibilities) {
            PlantDefinition candidate = snapshot.plants().get(species);
            if (candidate != null
                    && CropConditions.canGrow(level, target, candidate, inherited))
                return species;
        }
        return possibilities.getFirst(); // caller still rejects unsuitable offspring safely
    }
    /** Only mature, planted crop sticks may contribute; an empty gap ends a row. */
    private static Genome eligibleParent(ServerLevel level, BlockPos pos,
                                         Map<String, PlantDefinition> plants) {
        if (!level.hasChunkAt(pos)
                || !(level.getBlockEntity(pos) instanceof CropSticksBlockEntity source)
                || source.isRemoved()) return null;
        BlockState sourceState = level.getBlockState(pos);
        // A mature-looking genome in an out-of-sync BlockEntity is not a
        // physical parent. Only a planted, single-layer crop can contribute.
        if (!sourceState.is(CropBlocks.CROP_STICKS.get())
                || !sourceState.getValue(CropSticksBlock.PLANTED)
                || sourceState.getValue(CropSticksBlock.CROSS)) return null;
        Genome genome = source.genome();
        if (genome == null) return null;
        PlantDefinition plant = plants.get(genome.plantId());
        return plant != null && source.isMature(plant)
                && CropConditions.canGrow(level, pos, plant, genome) ? genome : null;
    }

}

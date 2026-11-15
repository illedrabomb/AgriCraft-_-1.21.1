package dev.illedrabomb.cropgenetics.data;

import java.util.Map;

/**
 * Published only after each resource reload has parsed successfully.
 * Each collection is individually atomic; crop-gameplay wiring and
 * cross-reference validation are a subsequent task.
 */
public final class AgriPlantsRegistry {
    private volatile Map<String, PlantDefinition> plants = Map.of();
    private volatile Map<String, MutationDefinition> mutations = Map.of();

    public Map<String, PlantDefinition> plants() { return plants; }
    public Map<String, MutationDefinition> mutations() { return mutations; }

    public void replacePlants(Map<String, PlantDefinition> next) { plants = Map.copyOf(next); }
    public void replaceMutations(Map<String, MutationDefinition> next) { mutations = Map.copyOf(next); }
}

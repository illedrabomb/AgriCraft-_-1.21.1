package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Objects;

/** Species mutation, independent from numeric gene inheritance. */
public record MutationDefinition(String childPlantId, String parentA, String parentB,
                                 double chance, List<String> allowedBiomes,
                                 List<String> blockedBiomes, List<String> allowedDimensions,
                                 List<String> blockedDimensions, EnvironmentFilter environment,
                                 SeasonRule seasonRule, SoilChemistryRule soilChemistry) {
    /** Existing unconditional mutations retain their original behavior. */
    public MutationDefinition(String childPlantId, String parentA, String parentB, double chance) {
        this(childPlantId, parentA, parentB, chance, List.of(), List.of(), List.of(), List.of(), EnvironmentFilter.UNRESTRICTED, SeasonRule.UNRESTRICTED, SoilChemistryRule.UNRESTRICTED);
    }

    /** Preserve existing location-only constructor. */
    public MutationDefinition(String childPlantId, String parentA, String parentB, double chance,
                              List<String> allowedBiomes, List<String> blockedBiomes,
                              List<String> allowedDimensions, List<String> blockedDimensions) {
        this(childPlantId, parentA, parentB, chance, allowedBiomes, blockedBiomes,
                allowedDimensions, blockedDimensions, EnvironmentFilter.UNRESTRICTED, SeasonRule.UNRESTRICTED);
    }

    /** Preserve callers that use location and fluid/height constraints. */
    public MutationDefinition(String childPlantId, String parentA, String parentB, double chance,
                              List<String> allowedBiomes, List<String> blockedBiomes,
                              List<String> allowedDimensions, List<String> blockedDimensions,
                              EnvironmentFilter environment) {
        this(childPlantId, parentA, parentB, chance, allowedBiomes, blockedBiomes,
                allowedDimensions, blockedDimensions, environment, SeasonRule.UNRESTRICTED, SoilChemistryRule.UNRESTRICTED);
    }

    /** Keep pre-chemistry seasonal mutations source-compatible. */
    public MutationDefinition(String childPlantId, String parentA, String parentB, double chance,
                              List<String> allowedBiomes, List<String> blockedBiomes,
                              List<String> allowedDimensions, List<String> blockedDimensions,
                              EnvironmentFilter environment, SeasonRule seasonRule) {
        this(childPlantId, parentA, parentB, chance, allowedBiomes, blockedBiomes,
                allowedDimensions, blockedDimensions, environment, seasonRule,
                SoilChemistryRule.UNRESTRICTED);
    }

    public MutationDefinition {
        Objects.requireNonNull(soilChemistry);
        Objects.requireNonNull(seasonRule);
        Objects.requireNonNull(environment);
        Objects.requireNonNull(childPlantId);
        Objects.requireNonNull(parentA);
        Objects.requireNonNull(parentB);
        for (String id : new String[] {childPlantId, parentA, parentB}) {
            checkId(id);
        }
        if (chance < 0 || chance > 1 || !Double.isFinite(chance))
            throw new IllegalArgumentException("Mutation chance must be in [0, 1]");
        allowedBiomes = List.copyOf(allowedBiomes);
        blockedBiomes = List.copyOf(blockedBiomes);
        allowedDimensions = List.copyOf(allowedDimensions);
        blockedDimensions = List.copyOf(blockedDimensions);
        validate(allowedBiomes, "allowed_biomes");
        validate(blockedBiomes, "blocked_biomes");
        validate(allowedDimensions, "allowed_dimensions");
        validate(blockedDimensions, "blocked_dimensions");
        if (allowedBiomes.stream().anyMatch(blockedBiomes::contains)
                || allowedDimensions.stream().anyMatch(blockedDimensions::contains))
            throw new IllegalArgumentException("Mutation location allow/block lists overlap");
    }

    private static void validate(List<String> list, String name) {
        if (list.size() > 64 || list.stream().distinct().count() != list.size())
            throw new IllegalArgumentException(name + " requires <=64 distinct values");
        list.forEach(MutationDefinition::checkId);
    }

    private static void checkId(String id) {
        if (id == null || id.length() > 128
                || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Invalid mutation identifier: " + id);
    }

    public boolean matchesParents(String a, String b) {
        return (parentA.equals(a) && parentB.equals(b))
                || (parentA.equals(b) && parentB.equals(a));
    }
}

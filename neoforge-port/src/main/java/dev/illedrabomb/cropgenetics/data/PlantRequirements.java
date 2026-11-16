package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Explicit crop environment requirements for the NeoForge port.
 * Biome/dimension allow- and deny-lists use exact registry IDs, not tags.
 * Legacy soil chemistry, seasons and Strength-based bypasses are NOT inferred.
 */
public record PlantRequirements(int minLight, int maxLight, Optional<String> soilBlock,
                                Optional<String> soilTag, int lightTolerance,
                                List<String> allowedBiomes, List<String> blockedBiomes,
                                List<String> allowedDimensions, List<String> blockedDimensions,
                                EnvironmentFilter environment, SeasonRule seasonRule,
                                SoilChemistryRule soilChemistry) {
    /** Existing callers keep unrestricted biome/dimension behavior. */
    public PlantRequirements(int minLight, int maxLight, Optional<String> soilBlock,
                             Optional<String> soilTag, int lightTolerance) {
        this(minLight, maxLight, soilBlock, soilTag, lightTolerance,
                List.of(), List.of(), List.of(), List.of(), EnvironmentFilter.UNRESTRICTED, SeasonRule.UNRESTRICTED, SoilChemistryRule.UNRESTRICTED);
    }

    /** Preserve constructor used by the existing biome/dimension datapack parser. */
    public PlantRequirements(int minLight, int maxLight, Optional<String> soilBlock,
                             Optional<String> soilTag, int lightTolerance,
                             List<String> allowedBiomes, List<String> blockedBiomes,
                             List<String> allowedDimensions, List<String> blockedDimensions) {
        this(minLight, maxLight, soilBlock, soilTag, lightTolerance, allowedBiomes,
                blockedBiomes, allowedDimensions, blockedDimensions, EnvironmentFilter.UNRESTRICTED, SeasonRule.UNRESTRICTED);
    }

    /** Preserve caller compatibility with pre-season requirements. */
    public PlantRequirements(int minLight, int maxLight, Optional<String> soilBlock,
                             Optional<String> soilTag, int lightTolerance,
                             List<String> allowedBiomes, List<String> blockedBiomes,
                             List<String> allowedDimensions, List<String> blockedDimensions,
                             EnvironmentFilter environment) {
        this(minLight, maxLight, soilBlock, soilTag, lightTolerance,
                allowedBiomes, blockedBiomes, allowedDimensions, blockedDimensions,
                environment, SeasonRule.UNRESTRICTED, SoilChemistryRule.UNRESTRICTED);
    }

    /** Preserve the pre-chemistry constructor for existing datapacks and callers. */
    public PlantRequirements(int minLight, int maxLight, Optional<String> soilBlock,
                             Optional<String> soilTag, int lightTolerance,
                             List<String> allowedBiomes, List<String> blockedBiomes,
                             List<String> allowedDimensions, List<String> blockedDimensions,
                             EnvironmentFilter environment, SeasonRule seasonRule) {
        this(minLight, maxLight, soilBlock, soilTag, lightTolerance, allowedBiomes,
                blockedBiomes, allowedDimensions, blockedDimensions, environment,
                seasonRule, SoilChemistryRule.UNRESTRICTED);
    }

    public PlantRequirements {
        Objects.requireNonNull(soilChemistry);
        Objects.requireNonNull(seasonRule);
        Objects.requireNonNull(environment);
        Objects.requireNonNull(soilBlock);
        Objects.requireNonNull(soilTag);
        allowedBiomes = List.copyOf(allowedBiomes);
        blockedBiomes = List.copyOf(blockedBiomes);
        allowedDimensions = List.copyOf(allowedDimensions);
        blockedDimensions = List.copyOf(blockedDimensions);
        if (lightTolerance < 0 || lightTolerance > 15)
            throw new IllegalArgumentException("Light tolerance must be within 0..15");
        if (minLight < 0 || maxLight > 15 || minLight > maxLight)
            throw new IllegalArgumentException("Light range must be within 0..15");
        if (soilBlock.isPresent() == soilTag.isPresent())
            throw new IllegalArgumentException("Specify exactly one of soil_block or soil_tag");
        soilBlock.ifPresent(PlantRequirements::checkId);
        soilTag.ifPresent(PlantRequirements::checkId);
        validateIds(allowedBiomes, "allowed_biomes");
        validateIds(blockedBiomes, "blocked_biomes");
        validateIds(allowedDimensions, "allowed_dimensions");
        validateIds(blockedDimensions, "blocked_dimensions");
        if (allowedBiomes.stream().anyMatch(blockedBiomes::contains)
                || allowedDimensions.stream().anyMatch(blockedDimensions::contains))
            throw new IllegalArgumentException("Biome/dimension allow and block lists must not overlap");
    }

    private static void validateIds(List<String> list, String label) {
        if (list.size() > 64 || list.stream().distinct().count() != list.size())
            throw new IllegalArgumentException(label + ": max 64 unique IDs");
        list.forEach(PlantRequirements::checkId);
    }

    private static void checkId(String value) {
        if (value == null || value.length() > 128
                || !value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Invalid environment identifier: " + value);
    }

    public static PlantRequirements defaultFarmland() {
        return new PlantRequirements(9, 15, Optional.of("minecraft:farmland"), Optional.empty(), 0);
    }
}

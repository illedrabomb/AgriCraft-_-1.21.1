package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.EnvironmentFilter;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exact soil restrictions; Strength only softens configured light deviations.
 * Never treat unknown modded soil chemistry or unsupported rules as satisfied.
 */
public final class CropConditions {
    private CropConditions() {}

    public static boolean matchesSoil(LevelReader level, BlockPos pos, PlantRequirements requirements) {
        // A crop may be at a chunk boundary; do not load a neighboring chunk
        // just to inspect the soil during an ordinary random tick.
        if (!isLoadedForSiteCheck(level, pos.below())) return false;
        BlockState soil = level.getBlockState(pos.below());
        if (requirements.soilBlock().isPresent()) {
            ResourceLocation id = ResourceLocation.tryParse(requirements.soilBlock().get());
            return id != null && BuiltInRegistries.BLOCK.getOptional(id)
                    .map(soil::is).orElse(false);
        }
        if (requirements.soilTag().isPresent()) {
            ResourceLocation id = ResourceLocation.tryParse(requirements.soilTag().get());
            return id != null && soil.is(TagKey.create(Registries.BLOCK, id));
        }
        return false;
    }

    /** A missing neighbor means 'not currently growable', not 'load it'. */
    private static boolean isLoadedForSiteCheck(LevelReader level, BlockPos pos) {
        return !(level instanceof ServerLevel server) || server.hasChunkAt(pos);
    }

    /**
     * Species-specific Vanilla planting rules within our crop-stick system.
     * These do not emulate stacked Vanilla blocks, only their growing sites.
     */
    public static boolean matchesPlantingSite(LevelReader level, BlockPos pos,
                                              PlantDefinition plant) {
        // The active datapack ALWAYS chooses the allowed supporting blocks.
        // Built-in soil families are represented by cropgenetics block tags;
        // custom datapacks can replace the tag or specify a single soil_block.
        if (!matchesSoil(level, pos, plant.requirements())) return false;

        if ("minecraft:sugar_cane_plant".equals(plant.id())) {
            // The additional Vanilla-like water requirement is independent
            // from the declared supporting soil.
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos besideSoil = pos.below().relative(direction);
                if (!isLoadedForSiteCheck(level, besideSoil)) continue;
                if (level.getFluidState(besideSoil).is(FluidTags.WATER)
                        || level.getBlockState(besideSoil).is(Blocks.FROSTED_ICE)) {
                    return true;
                }
            }
            return false;
        }
        if ("minecraft:cactus_plant".equals(plant.id())) {
            // Soil comes from the datapack; neighboring obstruction remains
            // a cactus-specific environmental constraint.
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(direction);
                // Unknown neighboring space must not be treated as empty.
                if (!isLoadedForSiteCheck(level, neighbor)) return false;
                if (level.getBlockState(neighbor).isSolid()
                        || !level.getFluidState(neighbor).isEmpty()) return false;
            }
            return true;
        }
        if ("minecraft:brown_mushroom_plant".equals(plant.id())
                || "minecraft:red_mushroom_plant".equals(plant.id())) {
            BlockState soil = level.getBlockState(pos.below());
            // Bright-light exemption is limited to these known substrates.
            // Other configured soils still require low light.
            return soil.is(Blocks.MYCELIUM) || soil.is(Blocks.PODZOL)
                    || soil.is(Blocks.CRIMSON_NYLIUM) || soil.is(Blocks.WARPED_NYLIUM)
                    || level.getRawBrightness(pos, 0) < 13;
        }
        // Bamboo and berries have no extra environmental rule beyond their
        // datapack soil and light restrictions.
        return true;
    }

    /**
     * Exact biome and dimension IDs are optional and intentionally strict.
     * Missing/unresolved biome IDs and non-Level readers are not treated as a
     * positive match when restrictions are configured.
     */
    public static boolean matchesLocation(LevelReader level, BlockPos pos,
                                          PlantRequirements requirements) {
        return matchesLocation(level, pos,
                requirements.allowedBiomes(), requirements.blockedBiomes(),
                requirements.allowedDimensions(), requirements.blockedDimensions());
    }

    /** Reusable exact location predicate for conditional mutation definitions. */
    public static boolean matchesLocation(LevelReader level, BlockPos pos,
                                          java.util.List<String> allowedBiomes,
                                          java.util.List<String> blockedBiomes,
                                          java.util.List<String> allowedDimensions,
                                          java.util.List<String> blockedDimensions) {
        boolean checkBiome = !allowedBiomes.isEmpty() || !blockedBiomes.isEmpty();
        if (checkBiome) {
            String biomeId = level.getBiome(pos).unwrapKey()
                    .map(key -> key.location().toString()).orElse(null);
            if (biomeId == null
                    || (!allowedBiomes.isEmpty()
                            && !allowedBiomes.contains(biomeId))
                    || blockedBiomes.contains(biomeId))
                return false;
        }
        boolean checkDimension = !allowedDimensions.isEmpty()
                || !blockedDimensions.isEmpty();
        if (checkDimension) {
            if (!(level instanceof Level world)) return false;
            String dimensionId = world.dimension().location().toString();
            if ((!allowedDimensions.isEmpty()
                    && !allowedDimensions.contains(dimensionId))
                    || blockedDimensions.contains(dimensionId))
                return false;
        }
        return true;
    }

    /** Apply exact fluid-state ID and inclusive Y bounds at the crop position. */
    public static boolean matchesEnvironment(LevelReader level, BlockPos pos, EnvironmentFilter filter) {
        if (filter.minY().isPresent() && pos.getY() < filter.minY().get()) return false;
        if (filter.maxY().isPresent() && pos.getY() > filter.maxY().get()) return false;
        if (filter.fluidId().isPresent()) {
            ResourceLocation actual = BuiltInRegistries.FLUID.getKey(level.getFluidState(pos).getType());
            return actual != null && filter.fluidId().get().equals(actual.toString());
        }
        return true;
    }

    /** No provider means seasons do not restrict crops; authoritative check is server-side. */
    public static boolean matchesSeason(LevelReader level, BlockPos pos,
                                        java.util.List<String> allowedSeasons) {
        if (allowedSeasons.isEmpty()) return true;
        if (level instanceof net.minecraft.server.level.ServerLevel server)
            return SeasonAccess.matches(server, pos, allowedSeasons);
        // Avoid client false negatives when the season provider is server-only.
        return SeasonAccess.matchesClient(allowedSeasons);
    }

    /** Soil chemistry is sampled from the supporting block, never guessed from farmland. */
    public static boolean matchesChemistry(LevelReader level, BlockPos pos,
                                           SoilChemistryRule rule, int strength) {
        if (!rule.configured()) return true;
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) {
            // Client cannot authorize a chemistry-dependent placement.
            // The server remains authoritative and rejects missing samples.
            return true;
        }
        return SoilChemistryAccess.matches(server, pos.below(), rule, strength);
    }

    /**
     * 0.0 blocks growth; values above 1.0 may boost growth. A plant with Strength 1 has
     * no tolerance. Strength 10 reaches the configured maximum light deficit.
     * Soil remains a hard condition regardless of strength.
     */
    public static double growthFactor(LevelReader level, BlockPos pos,
                                      PlantDefinition plant, Genome genome) {
        PlantRequirements requirements = plant.requirements();
        int strength = genome.strength();
        if (!matchesPlantingSite(level, pos, plant)
                || !matchesLocation(level, pos, requirements)
                || !matchesEnvironment(level, pos, requirements.environment())
                || !matchesSeason(level, pos, requirements.seasonRule().allowedSeasons())
                || !matchesChemistry(level, pos, requirements.soilChemistry(), strength)) return 0.0D;

        int light = level.getRawBrightness(pos, 0);
        int deficit = Math.max(0, Math.max(requirements.minLight() - light,
                light - requirements.maxLight()));
        // Original AgriPlants soil growth_modifier is a multiplier, applied
        // even when light is optimal. Per-position chemistry overrides do not
        // overwrite this block-type growth property.
        double soilModifier = dev.illedrabomb.cropgenetics.CropGeneticsMod.soilProfiles()
                .growthModifier(level.getBlockState(pos.below()));
        if (deficit == 0) return soilModifier;

        int allowed = (requirements.lightTolerance() * (strength - 1)) / 9;
        if (allowed <= 0 || deficit > allowed) return 0.0D;

        // Even tolerated conditions are suboptimal; high-strength plants
        // grow more slowly outside their preferred lighting.
        return soilModifier * (allowed + 1.0D - deficit) / (allowed + 1.0D);
    }

    public static boolean canGrow(LevelReader level, BlockPos pos,
                                  PlantDefinition plant, Genome genome) {
        return growthFactor(level, pos, plant, genome) > 0.0D;
    }
}

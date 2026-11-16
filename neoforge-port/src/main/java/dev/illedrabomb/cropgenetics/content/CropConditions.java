package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exact soil restrictions; Strength only softens configured light deviations.
 * Never treat unknown modded soil chemistry or unsupported rules as satisfied.
 */
public final class CropConditions {
    private CropConditions() {}

    public static boolean matchesSoil(LevelReader level, BlockPos pos, PlantRequirements requirements) {
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

    /**
     * Exact biome and dimension IDs are optional and intentionally strict.
     * Missing/unresolved biome IDs and non-Level readers are not treated as a
     * positive match when restrictions are configured.
     */
    public static boolean matchesLocation(LevelReader level, BlockPos pos,
                                          PlantRequirements requirements) {
        boolean checkBiome = !requirements.allowedBiomes().isEmpty()
                || !requirements.blockedBiomes().isEmpty();
        if (checkBiome) {
            String biomeId = level.getBiome(pos).unwrapKey()
                    .map(key -> key.location().toString()).orElse(null);
            if (biomeId == null
                    || (!requirements.allowedBiomes().isEmpty()
                            && !requirements.allowedBiomes().contains(biomeId))
                    || requirements.blockedBiomes().contains(biomeId))
                return false;
        }
        boolean checkDimension = !requirements.allowedDimensions().isEmpty()
                || !requirements.blockedDimensions().isEmpty();
        if (checkDimension) {
            if (!(level instanceof Level world)) return false;
            String dimensionId = world.dimension().location().toString();
            if ((!requirements.allowedDimensions().isEmpty()
                    && !requirements.allowedDimensions().contains(dimensionId))
                    || requirements.blockedDimensions().contains(dimensionId))
                return false;
        }
        return true;
    }

    /**
     * 0.0 blocks growth; 1.0 has no penalty. A plant with Strength 1 has
     * no tolerance. Strength 10 reaches the configured maximum light deficit.
     * Soil remains a hard condition regardless of strength.
     */
    public static double growthFactor(LevelReader level, BlockPos pos,
                                      PlantDefinition plant, Genome genome) {
        PlantRequirements requirements = plant.requirements();
        if (!matchesSoil(level, pos, requirements)
                || !matchesLocation(level, pos, requirements)) return 0.0D;

        int light = level.getRawBrightness(pos, 0);
        int deficit = Math.max(0, Math.max(requirements.minLight() - light,
                light - requirements.maxLight()));
        if (deficit == 0) return 1.0D;

        int strength = genome.strength().expressed(GenePair.TraitMode.MAX);
        int allowed = (requirements.lightTolerance() * (strength - 1)) / 9;
        if (allowed <= 0 || deficit > allowed) return 0.0D;

        // Even tolerated conditions are suboptimal; high-strength plants
        // grow more slowly outside their preferred lighting.
        return (allowed + 1.0D - deficit) / (allowed + 1.0D);
    }

    public static boolean canGrow(LevelReader level, BlockPos pos,
                                  PlantDefinition plant, Genome genome) {
        return growthFactor(level, pos, plant, genome) > 0.0D;
    }
}

package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
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
     * 0.0 blocks growth; 1.0 has no penalty. A plant with Strength 1 has
     * no tolerance. Strength 10 reaches the configured maximum light deficit.
     * Soil remains a hard condition regardless of strength.
     */
    public static double growthFactor(LevelReader level, BlockPos pos,
                                      PlantDefinition plant, Genome genome) {
        PlantRequirements requirements = plant.requirements();
        if (!matchesPlantingSite(level, pos, plant)) return 0.0D;

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

package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Strictly validate known soil and light rules; optional modded soils use block IDs or tags. */
public final class CropConditions {
    private CropConditions() {}

    public static boolean matchesSoil(LevelReader level, BlockPos pos, PlantRequirements requirement) {
        BlockState soil = level.getBlockState(pos.below());
        if (requirement.soilBlock().isPresent()) {
            ResourceLocation id = ResourceLocation.tryParse(requirement.soilBlock().get());
            return id != null && BuiltInRegistries.BLOCK.getOptional(id)
                    .map(soil::is).orElse(false);
        }
        if (requirement.soilTag().isPresent()) {
            ResourceLocation id = ResourceLocation.tryParse(requirement.soilTag().get());
            return id != null && soil.is(TagKey.create(Registries.BLOCK, id));
        }
        return false;
    }

    public static boolean canGrow(LevelReader level, BlockPos pos, PlantDefinition plant) {
        if (!matchesSoil(level, pos, plant.requirements())) return false;
        int light = level.getRawBrightness(pos, 0);
        return light >= plant.requirements().minLight()
                && light <= plant.requirements().maxLight();
    }
}

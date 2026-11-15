package dev.illedrabomb.cropgenetics.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves explicit item and soil-block IDs against the live game registries.
 * Called from the server reload apply phase, not from background preparation.
 * Tags are datapack-managed; their syntax is validated by PlantRequirements,
 * but their membership is intentionally not guessed here.
 */
public final class PlantRegistryValidator {
    private PlantRegistryValidator() {}

    public static List<String> errors(PlantDefinition plant) {
        List<String> problems = new ArrayList<>();

        for (String seed : plant.seedItems()) {
            ResourceLocation id = ResourceLocation.tryParse(seed);
            if (id == null || BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {
                problems.add("Unknown seed item: " + seed);
            }
        }

        for (PlantDefinition.HarvestProduct product : plant.harvestProducts()) {
            ResourceLocation id = ResourceLocation.tryParse(product.itemId());
            if (id == null || BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {
                problems.add("Unknown harvest item: " + product.itemId());
            }
        }

        plant.requirements().soilBlock().ifPresent(value -> {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null || BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) {
                problems.add("Unknown soil block: " + value);
            }
        });

        return List.copyOf(problems);
    }
}

package dev.illedrabomb.cropgenetics.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.packs.resources.ResourceManager;

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
            } else if (BuiltInRegistries.ITEM.getOptional(id).orElseThrow() == Items.AIR) {
                problems.add("Seed item cannot be air: " + seed);
            }
        }

        for (PlantDefinition.HarvestProduct product : plant.harvestProducts()) {
            ResourceLocation id = ResourceLocation.tryParse(product.itemId());
            if (id == null || BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {
                problems.add("Unknown harvest item: " + product.itemId());
            } else if (BuiltInRegistries.ITEM.getOptional(id).orElseThrow() == Items.AIR) {
                problems.add("Harvest item cannot be air: " + product.itemId());
            }
        }

        plant.requirements().soilBlock().ifPresent(value -> {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null || BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) {
                problems.add("Unknown soil block: " + value);
            } else if (BuiltInRegistries.BLOCK.getOptional(id).orElseThrow() == Blocks.AIR) {
                problems.add("Soil block cannot be air: " + value);
            }
        });

        return List.copyOf(problems);
    }

    /**
     * Advisory only. In 1.21.1 block tag JSON resides under tags/block/,
     * not the pre-1.21 tags/blocks/ folder. Resource packs/mods may supply
     * tags through custom mechanisms, so absence is not an invalid plant.
     */
    public static List<String> tagWarnings(PlantDefinition plant, ResourceManager manager) {
        List<String> warnings = new ArrayList<>();
        plant.requirements().soilTag().ifPresent(tag -> {
            ResourceLocation id = ResourceLocation.tryParse(tag);
            if (id == null) return; // PlantRequirements already validates syntax.
            ResourceLocation file = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(), "tags/block/" + id.getPath() + ".json");
            if (manager.getResource(file).isEmpty()) {
                warnings.add("No block-tag JSON found for #" + tag
                        + " (expected " + file + "; runtime tag may still exist)");
            }
        });
        return List.copyOf(warnings);
    }
}

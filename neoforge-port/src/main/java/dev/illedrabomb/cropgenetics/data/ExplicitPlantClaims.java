package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared, fail-closed inventory of explicit datapack identities and items.
 *
 * Add-on discovery must never infer a seed or produce claim when an explicit
 * plant file may already own it. Collect claims from EVERY resource, including
 * disabled or invalid definitions, before invoking any optional adapter.
 */
public record ExplicitPlantClaims(Set<String> species, Set<String> seeds,
                                  Set<String> produce, boolean safe) {
    public ExplicitPlantClaims {
        species = Set.copyOf(species);
        seeds = Set.copyOf(seeds);
        produce = Set.copyOf(produce);
    }

    public static ExplicitPlantClaims collect(
            Map<ResourceLocation, JsonElement> definitions,
            List<CropReloadReport.Entry> readErrors) {
        Set<String> species = new HashSet<>();
        Set<String> seeds = new HashSet<>();
        Set<String> produce = new HashSet<>();
        boolean safe = readErrors.stream().noneMatch(error ->
                error.kind() == CropReloadReport.Kind.PLANT
                        && error.source().getPath().startsWith("cropgenetics/plants/"));

        for (JsonElement element : definitions.values()) {
            try {
                JsonObject plant = element.getAsJsonObject();
                if (!plant.has("id") || !plant.get("id").isJsonPrimitive()) {
                    safe = false;
                    continue;
                }
                species.add(plant.get("id").getAsString());

                if (plant.has("seed_items")) {
                    for (JsonElement item : plant.getAsJsonArray("seed_items")) {
                        JsonObject seed = item.getAsJsonObject();
                        if (!seed.has("object") || !seed.get("object").isJsonPrimitive()) {
                            safe = false;
                            continue;
                        }
                        seeds.add(seed.get("object").getAsString());
                    }
                }

                if (plant.has("products")) {
                    JsonObject products = plant.getAsJsonObject("products");
                    if (!products.has("products") || !products.get("products").isJsonArray()) {
                        safe = false;
                        continue;
                    }
                    for (JsonElement item : products.getAsJsonArray("products")) {
                        JsonObject result = item.getAsJsonObject();
                        if (!result.has("object") || !result.get("object").isJsonPrimitive()) {
                            safe = false;
                            continue;
                        }
                        produce.add(result.get("object").getAsString());
                    }
                }
            } catch (RuntimeException ignored) {
                // Missing/malformed identity and item claims must never be
                // replaced by inferred add-on registrations.
                safe = false;
            }
        }
        return new ExplicitPlantClaims(species, seeds, produce, safe);
    }
}

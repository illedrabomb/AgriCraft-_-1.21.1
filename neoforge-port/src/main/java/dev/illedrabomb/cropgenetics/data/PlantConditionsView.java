package dev.illedrabomb.cropgenetics.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Public, immutable projection of one authoritative server catalogue for a
 * future client information view. Never contains genomes, player discoveries,
 * inventory data, mutations or the reload diagnostics.
 *
 * <p>This is deliberately not a network payload: a separate, versioned and
 * size-checked wire codec must be reviewed before exposing it to clients.
 */
public record PlantConditionsView(List<Entry> plants) {
    public static final int MAX_PLANTS = 512;

    public enum SiteRule {
        NONE, SUGAR_CANE_ADJACENT_WATER, CACTUS_CLEAR_SIDES,
        MUSHROOM_DARKNESS_EXEMPTION
    }

    public record Entry(String plantId, List<String> seedItems,
                        PlantRequirements requirements, SiteRule siteRule) {
        public Entry {
            Objects.requireNonNull(plantId);
            seedItems = List.copyOf(seedItems);
            Objects.requireNonNull(requirements);
            Objects.requireNonNull(siteRule);
        }
    }

    public PlantConditionsView {
        plants = List.copyOf(plants);
        if (plants.size() > MAX_PLANTS)
            throw new IllegalArgumentException("Too many plant conditions for public view");
    }

    /** Capture a single published snapshot, never mix two reload generations. */
    public static PlantConditionsView from(AgriPlantsRegistry.Snapshot snapshot) {
        Objects.requireNonNull(snapshot);
        if (snapshot.plants().size() > MAX_PLANTS)
            throw new IllegalArgumentException("Server has more than " + MAX_PLANTS
                    + " plants; refusing incomplete client conditions");

        List<Entry> entries = new ArrayList<>();
        snapshot.plants().values().stream()
                .sorted(Comparator.comparing(PlantDefinition::id))
                .forEach(plant -> {
                    // Only unambiguous reverse mappings are advertised as
                    // plantable ordinary items. Generic genetic seeds have
                    // their species in their own data component instead.
                    List<String> validSeeds = plant.seedItems().stream()
                            .filter(seed -> plant.id().equals(snapshot.seedToPlant().get(seed)))
                            .distinct().sorted().toList();
                    entries.add(new Entry(plant.id(), validSeeds,
                            plant.requirements(), siteRule(plant.id())));
                });
        return new PlantConditionsView(entries);
    }

    private static SiteRule siteRule(String id) {
        return switch (id) {
            case "minecraft:sugar_cane_plant" -> SiteRule.SUGAR_CANE_ADJACENT_WATER;
            case "minecraft:cactus_plant" -> SiteRule.CACTUS_CLEAR_SIDES;
            case "minecraft:brown_mushroom_plant", "minecraft:red_mushroom_plant" ->
                    SiteRule.MUSHROOM_DARKNESS_EXEMPTION;
            default -> SiteRule.NONE;
        };
    }
}

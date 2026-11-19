package dev.illedrabomb.cropgenetics.content;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeedSpeciesCatalogTest {
    /**
     * Prevent an accidental model copy/paste from making two species appear
     * as the same item in JEI, the analyzer and the creative inventory.
     * Some plants deliberately use their vanilla fruit or block icon because
     * vanilla has no separate seed item for them.
     */
    @Test
    void seedIconsFollowTheirVanillaSpecies() throws Exception {
        var expectedIcons = java.util.Map.ofEntries(
                java.util.Map.entry("carrot", "minecraft:item/carrot"),
                java.util.Map.entry("potato", "minecraft:item/potato"),
                java.util.Map.entry("beetroot", "minecraft:item/beetroot_seeds"),
                java.util.Map.entry("nether_wart", "minecraft:item/nether_wart"),
                java.util.Map.entry("melon", "minecraft:item/melon_seeds"),
                java.util.Map.entry("pumpkin", "minecraft:item/pumpkin_seeds"),
                java.util.Map.entry("cornflower", "minecraft:block/cornflower"),
                java.util.Map.entry("poppy", "minecraft:block/poppy"),
                java.util.Map.entry("dandelion", "minecraft:block/dandelion"),
                java.util.Map.entry("cactus", "minecraft:block/cactus_side"),
                java.util.Map.entry("sugar_cane", "minecraft:item/sugar_cane"),
                java.util.Map.entry("bamboo", "minecraft:item/bamboo"),
                java.util.Map.entry("sweet_berry", "minecraft:item/sweet_berries"),
                java.util.Map.entry("brown_mushroom", "minecraft:block/brown_mushroom"),
                java.util.Map.entry("red_mushroom", "minecraft:block/red_mushroom"));
        for (var entry : expectedIcons.entrySet()) {
            String path = "assets/cropgenetics/models/item/genetic_seed_"
                    + entry.getKey() + ".json";
            try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(stream, path);
                var json = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                assertEquals(entry.getValue(),
                        json.getAsJsonObject("textures").get("layer0").getAsString(),
                        "Wrong visual identity for " + entry.getKey());
            }
        }
    }

    /**
     * The client seed_species property is index / 16, and Minecraft chooses
     * the last matching item-model override. Verify every JSON threshold and
     * target model against the catalog used by JEI and the creative tab.
     */
    @Test
    void allBuiltInSeedModelsMatchTheSharedCatalog() throws Exception {
        List<String> species = SeedSpeciesCatalog.DEFAULT_SPECIES;
        assertEquals(16, species.size());
        assertEquals(16, new HashSet<>(species).size(), "Duplicate JEI subtype species");

        List<String> modelNames = List.of(
                "wheat", "carrot", "potato", "beetroot", "nether_wart",
                "melon", "pumpkin", "cornflower", "poppy", "dandelion",
                "cactus", "sugar_cane", "bamboo", "sweet_berry",
                "brown_mushroom", "red_mushroom");
        assertEquals("minecraft:wheat_plant", species.get(0));

        var loader = getClass().getClassLoader();
        try (var stream = loader.getResourceAsStream(
                "assets/cropgenetics/models/item/genetic_seed.json")) {
            assertNotNull(stream);
            var root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals("minecraft:item/wheat_seeds",
                    root.getAsJsonObject("textures").get("layer0").getAsString());
            var overrides = root.getAsJsonArray("overrides");
            assertEquals(species.size() - 1, overrides.size());
            for (int i = 1; i < species.size(); i++) {
                assertEquals("minecraft:" + modelNames.get(i)
                                .replace("beetroot", "beet_root") + "_plant",
                        species.get(i));
                var override = overrides.get(i - 1).getAsJsonObject();
                assertEquals((float) i / species.size(),
                        override.getAsJsonObject("predicate")
                                .get("cropgenetics:seed_species").getAsFloat());
                String model = "cropgenetics:item/genetic_seed_" + modelNames.get(i);
                assertEquals(model, override.get("model").getAsString());
                try (var modelStream = loader.getResourceAsStream(
                        "assets/cropgenetics/models/item/genetic_seed_"
                                + modelNames.get(i) + ".json")) {
                    assertNotNull(modelStream, "Missing model for " + species.get(i));
                    var modelJson = JsonParser.parseReader(
                            new InputStreamReader(modelStream, StandardCharsets.UTF_8))
                            .getAsJsonObject();
                    assertTrue(modelJson.getAsJsonObject("textures").has("layer0"),
                            "Missing item texture for " + species.get(i));
                }
            }
        }
    }
}

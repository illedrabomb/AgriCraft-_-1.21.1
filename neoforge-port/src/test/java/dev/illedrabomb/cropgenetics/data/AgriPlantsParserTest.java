package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgriPlantsParserTest {
    private static final String CARROT = """
        {"version":"1.18.2","enabled":true,"mods":["minecraft","agricraft"],
        "id":"minecraft:carrot_plant",
        "seed_items":[{"type":"item","object":"minecraft:carrot","useTag":false}],
        "stages":[2,4,6,8,10,12,14,16],"harvestStage":4,
        "growth_chance":0.75,"growth_bonus":0.025,"cloneable":true,
        "spread_chance":0.1,
        "products":{"products":[{"type":"item","object":"minecraft:carrot",
        "min":1,"max":4,"chance":1.0}]}}
        """;

    @Test
    void parsesOriginalCarrotPlant() {
        var plant = AgriPlantsParser.plant(JsonParser.parseString(CARROT).getAsJsonObject());
        assertEquals("minecraft:carrot_plant", plant.id());
        assertEquals("minecraft:carrot", plant.seedItems().getFirst());
        assertEquals(8, plant.stages().size());
        assertEquals(4, plant.harvestStage());
        assertEquals("minecraft:carrot", plant.harvestProducts().getFirst().itemId());
    }

    @Test
    void parsesOriginalMutationAndSupportsEitherParentOrder() {
        var mutation = AgriPlantsParser.mutation(JsonParser.parseString("""
            {"chance":0.75,"child":"minecraft:carrot_plant",
            "parent1":"minecraft:wheat_plant","parent2":"minecraft:potato_plant","conditions":[]}
            """).getAsJsonObject());
        assertEquals(0.75, mutation.chance());
        assertTrue(mutation.matchesParents("minecraft:potato_plant", "minecraft:wheat_plant"));
        assertFalse(mutation.matchesParents("minecraft:carrot_plant", "minecraft:wheat_plant"));
    }

    @Test
    void invalidStageAndConditionalMutationAreRejected() {
        String invalid = CARROT.replace("\"harvestStage\":4", "\"harvestStage\":18");
        assertThrows(IllegalArgumentException.class,
            () -> AgriPlantsParser.plant(JsonParser.parseString(invalid).getAsJsonObject()));
        assertThrows(RuntimeException.class, () -> AgriPlantsParser.mutation(JsonParser.parseString("""
            {"chance":0.5,"child":"minecraft:carrot_plant","parent1":"a:a","parent2":"b:b",
            "conditions":[{"type":"biome"}]}
            """).getAsJsonObject()));
    }
}

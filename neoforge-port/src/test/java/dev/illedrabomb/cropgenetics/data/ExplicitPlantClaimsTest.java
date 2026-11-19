package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExplicitPlantClaimsTest {
    private static final ResourceLocation SOURCE =
            ResourceLocation.fromNamespaceAndPath("test", "cropgenetics/plants/example.json");

    private static Map<ResourceLocation, JsonElement> definition(String json) {
        return Map.of(SOURCE, JsonParser.parseString(json));
    }

    @Test
    void reservesClaimsEvenForDisabledPlantDefinitions() {
        ExplicitPlantClaims claims = ExplicitPlantClaims.collect(definition("""
                {"id":"pamhc2crops:cotton_plant","enabled":false,
                 "seed_items":[{"object":"pamhc2crops:cottonitem"}],
                 "products":{"products":[{"object":"pamhc2crops:cottonitem"}]}}
                """), List.of());

        assertTrue(claims.safe());
        assertEquals(Set.of("pamhc2crops:cotton_plant"), claims.species());
        assertEquals(Set.of("pamhc2crops:cottonitem"), claims.seeds());
        assertEquals(Set.of("pamhc2crops:cottonitem"), claims.produce());
    }

    @Test
    void suspendsWhenClaimIdentityOrItemIsMalformed() {
        assertFalse(ExplicitPlantClaims.collect(
                definition("{\"seed_items\":[]}"), List.of()).safe());
        assertFalse(ExplicitPlantClaims.collect(
                definition("{\"id\":\"test:a\",\"seed_items\":[{}]}"),
                List.of()).safe());
        assertFalse(ExplicitPlantClaims.collect(
                definition("{\"id\":\"test:a\",\"products\":{\"products\":{}}}"),
                List.of()).safe());
    }

    @Test
    void unreadablePlantFileSuspendsFallbackButMutationErrorDoesNot() {
        CropReloadReport.Entry unreadable = new CropReloadReport.Entry(
                CropReloadReport.Kind.PLANT, SOURCE, "", CropReloadReport.Status.INVALID,
                "Unreadable JSON");
        assertFalse(ExplicitPlantClaims.collect(Map.of(), List.of(unreadable)).safe());

        CropReloadReport.Entry mutation = new CropReloadReport.Entry(
                CropReloadReport.Kind.MUTATION, SOURCE, "", CropReloadReport.Status.INVALID,
                "Bad mutation JSON");
        assertTrue(ExplicitPlantClaims.collect(Map.of(), List.of(mutation)).safe());
    }

    @Test
    void unrecognizedJsonStructureFailsClosed() {
        assertFalse(ExplicitPlantClaims.collect(
                definition("[]"), List.of()).safe());
    }
}

package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Predicate;

/**
 * Optional crop definitions must disappear cleanly when their source mod is
 * absent. Keep the legacy "agricraft" dependency alias for imported datapacks.
 */
final class CropModDependencyRules {
    private CropModDependencyRules() {}

    /** Returns the first missing mod ID, or null if all are installed. */
    static String firstMissing(JsonObject json, Predicate<String> loaded) {
        if (!json.has("mods")) return null;
        for (JsonElement element : json.getAsJsonArray("mods")) {
            String modId = element.getAsString();
            if (!"agricraft".equals(modId) && !loaded.test(modId))
                return modId;
        }
        return null;
    }
}

package dev.illedrabomb.cropgenetics.addon.farmersdelight;

/** The three reviewed generic Farmer's Delight Crop Sticks candidates. */
public final class FarmersDelightSpeciesDisplayPolicy {
    private FarmersDelightSpeciesDisplayPolicy() {}

    public static String produceForSpecies(String species) {
        if (species == null) return null;
        return switch (species) {
            case "farmersdelight:cabbage_plant" -> "farmersdelight:cabbage";
            case "farmersdelight:tomato_plant" -> "farmersdelight:tomato";
            case "farmersdelight:onion_plant" -> "farmersdelight:onion";
            default -> null;
        };
    }
}

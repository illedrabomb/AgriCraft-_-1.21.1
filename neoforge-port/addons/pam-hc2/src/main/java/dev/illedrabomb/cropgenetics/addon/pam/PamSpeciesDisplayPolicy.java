package dev.illedrabomb.cropgenetics.addon.pam;

import net.minecraft.resources.ResourceLocation;

/** Display-only naming convention for Pam's reviewed crop item identities. */
public final class PamSpeciesDisplayPolicy {
    private PamSpeciesDisplayPolicy() {}

    public static String produceForSpecies(String species) {
        ResourceLocation key = ResourceLocation.tryParse(species);
        if (key == null || !key.getNamespace().equals("pamhc2crops")
                || !key.getPath().endsWith("_plant")) return null;
        String crop = key.getPath().substring(
                0, key.getPath().length() - "_plant".length());
        return crop.matches("[a-z][a-z0-9_]*")
                ? "pamhc2crops:" + crop + "item" : null;
    }
}

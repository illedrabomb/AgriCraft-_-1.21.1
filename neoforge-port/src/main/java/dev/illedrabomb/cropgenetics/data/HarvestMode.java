package dev.illedrabomb.cropgenetics.data;

/** Harvest lifecycle; GOURD produces one block fruit after the stem matures. */
public enum HarvestMode {
    STANDARD, GOURD;

    public static HarvestMode parse(String name) {
        return switch (name) {
            case "standard" -> STANDARD;
            case "gourd" -> GOURD;
            default -> throw new IllegalArgumentException("Unknown harvest mode: " + name);
        };
    }
}

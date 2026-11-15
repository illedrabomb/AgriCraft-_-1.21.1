package dev.illedrabomb.cropgenetics.content;

import net.minecraft.util.StringRepresentable;

/** Visual-only palette; the real species always lives in the Genome. */
public enum CropAppearance implements StringRepresentable {
    GENERIC("generic"), WHEAT("wheat"), CARROT("carrot"), POTATO("potato");

    private final String name;
    CropAppearance(String name) { this.name = name; }

    @Override
    public String getSerializedName() { return name; }

    public static CropAppearance fromPlantId(String id) {
        return switch (id) {
            case "minecraft:wheat_plant" -> WHEAT;
            case "minecraft:carrot_plant" -> CARROT;
            case "minecraft:potato_plant" -> POTATO;
            default -> GENERIC;
        };
    }
}

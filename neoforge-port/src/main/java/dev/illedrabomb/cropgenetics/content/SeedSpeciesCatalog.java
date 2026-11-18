package dev.illedrabomb.cropgenetics.content;

import java.util.List;

/**
 * Stable built-in species order shared by the creative tab, JEI and
 * the seed_species item-model predicate. The index is part of the visual
 * resource contract: changes must be reflected in genetic_seed.json.
 */
public final class SeedSpeciesCatalog {
    private SeedSpeciesCatalog() {}

    public static final List<String> DEFAULT_SPECIES = List.of(
            "minecraft:wheat_plant",
            "minecraft:carrot_plant",
            "minecraft:potato_plant",
            "minecraft:beet_root_plant",
            "minecraft:nether_wart_plant",
            "minecraft:melon_plant",
            "minecraft:pumpkin_plant",
            "minecraft:cornflower_plant",
            "minecraft:poppy_plant",
            "minecraft:dandelion_plant",
            "minecraft:cactus_plant",
            "minecraft:sugar_cane_plant",
            "minecraft:bamboo_plant",
            "minecraft:sweet_berry_plant",
            "minecraft:brown_mushroom_plant",
            "minecraft:red_mushroom_plant");
    /** Known Vanilla entry items for JEI help and journal illustrations. */
    public static String vanillaSeedForSpecies(String id) {
        return switch (id) {
            case "minecraft:wheat_plant" -> "minecraft:wheat_seeds";
            case "minecraft:carrot_plant" -> "minecraft:carrot";
            case "minecraft:potato_plant" -> "minecraft:potato";
            case "minecraft:beet_root_plant" -> "minecraft:beetroot_seeds";
            case "minecraft:nether_wart_plant" -> "minecraft:nether_wart";
            case "minecraft:melon_plant" -> "minecraft:melon_seeds";
            case "minecraft:pumpkin_plant" -> "minecraft:pumpkin_seeds";
            case "minecraft:cornflower_plant" -> "minecraft:cornflower";
            case "minecraft:poppy_plant" -> "minecraft:poppy";
            case "minecraft:dandelion_plant" -> "minecraft:dandelion";
            case "minecraft:cactus_plant" -> "minecraft:cactus";
            case "minecraft:sugar_cane_plant" -> "minecraft:sugar_cane";
            case "minecraft:bamboo_plant" -> "minecraft:bamboo";
            case "minecraft:sweet_berry_plant" -> "minecraft:sweet_berries";
            case "minecraft:brown_mushroom_plant" -> "minecraft:brown_mushroom";
            case "minecraft:red_mushroom_plant" -> "minecraft:red_mushroom";
            default -> null;
        };
    }

}
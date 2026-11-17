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
}

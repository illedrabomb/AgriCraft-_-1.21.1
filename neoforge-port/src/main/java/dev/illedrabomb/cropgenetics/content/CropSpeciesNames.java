package dev.illedrabomb.cropgenetics.content;

import net.minecraft.network.chat.Component;

/** Human-readable, translatable crop species names shared by HUD and seeds. */
public final class CropSpeciesNames {
    private CropSpeciesNames() {}

    public static Component label(String id) {
        String key = switch (id) {
            case "minecraft:wheat_plant" -> "block.minecraft.wheat";
            case "minecraft:carrot_plant" -> "item.minecraft.carrot";
            case "minecraft:potato_plant" -> "item.minecraft.potato";
            case "minecraft:beet_root_plant" -> "item.minecraft.beetroot";
            case "minecraft:nether_wart_plant" -> "item.minecraft.nether_wart";
            case "minecraft:melon_plant" -> "block.minecraft.melon";
            case "minecraft:pumpkin_plant" -> "block.minecraft.pumpkin";
            case "minecraft:cornflower_plant" -> "block.minecraft.cornflower";
            case "minecraft:poppy_plant" -> "block.minecraft.poppy";
            case "minecraft:dandelion_plant" -> "block.minecraft.dandelion";
            case "minecraft:cactus_plant" -> "block.minecraft.cactus";
            case "minecraft:sugar_cane_plant" -> "item.minecraft.sugar_cane";
            case "minecraft:bamboo_plant" -> "item.minecraft.bamboo";
            case "minecraft:sweet_berry_plant" -> "item.minecraft.sweet_berries";
            case "minecraft:brown_mushroom_plant" -> "block.minecraft.brown_mushroom";
            case "minecraft:red_mushroom_plant" -> "block.minecraft.red_mushroom";
            default -> null;
        };
        if (key != null) return Component.translatable(key);
        return Component.literal(id.substring(id.indexOf(':') + 1)
                .replace("_plant", "").replace('_', ' '));
    }
}

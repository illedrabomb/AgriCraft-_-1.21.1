package dev.illedrabomb.cropgenetics.compat.jei;

import dev.illedrabomb.cropgenetics.data.PlantConditionsView;
import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Text is derived from the latest server rules, never from default species. */
public final class PlantConditionsText {
    private PlantConditionsText() {}

    public static List<Component> lines(PlantConditionsView.Entry plant) {
        var rules = plant.requirements();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("jei.cropgenetics.conditions.plant", plant.plantId()));
        if (rules.soilBlock().isPresent()) {
            String blockId = rules.soilBlock().orElseThrow();
            ResourceLocation key = ResourceLocation.tryParse(blockId);
            String name = key == null ? blockId : BuiltInRegistries.BLOCK.getOptional(key)
                    .map(block -> block.getName().getString()).orElse(blockId);
            lines.add(Component.translatable("jei.cropgenetics.conditions.soil_block", name, blockId));
        } else {
            lines.add(Component.translatable("jei.cropgenetics.conditions.soil_tag",
                    "#" + rules.soilTag().orElse("?")));
        }
        lines.add(Component.translatable("jei.cropgenetics.conditions.light",
                rules.minLight(), rules.maxLight()));
        if (rules.lightTolerance() > 0)
            lines.add(Component.translatable("jei.cropgenetics.conditions.tolerance", rules.lightTolerance()));

        switch (plant.siteRule()) {
            case SUGAR_CANE_ADJACENT_WATER -> lines.add(Component.translatable("jei.cropgenetics.conditions.sugar_cane"));
            case CACTUS_CLEAR_SIDES -> lines.add(Component.translatable("jei.cropgenetics.conditions.cactus"));
            case MUSHROOM_DARKNESS_EXEMPTION -> lines.add(Component.translatable("jei.cropgenetics.conditions.mushroom"));
            case NONE -> { }
        }

        addIds(lines, "biomes_allowed", rules.allowedBiomes());
        addIds(lines, "biomes_blocked", rules.blockedBiomes());
        addIds(lines, "dimensions_allowed", rules.allowedDimensions());
        addIds(lines, "dimensions_blocked", rules.blockedDimensions());
        var env = rules.environment();
        env.minY().ifPresent(y -> lines.add(Component.translatable("jei.cropgenetics.conditions.min_y", y)));
        env.maxY().ifPresent(y -> lines.add(Component.translatable("jei.cropgenetics.conditions.max_y", y)));
        env.fluidId().ifPresent(id -> lines.add(Component.translatable("jei.cropgenetics.conditions.fluid", id)));
        addIds(lines, "seasons", rules.seasonRule().allowedSeasons());
        addChemistry(lines, "humidity", rules.soilChemistry().humidity());
        addChemistry(lines, "acidity", rules.soilChemistry().acidity());
        addChemistry(lines, "nutrients", rules.soilChemistry().nutrients());
        lines.add(Component.translatable("jei.cropgenetics.conditions.server_note"));
        return List.copyOf(lines);
    }

    private static void addIds(List<Component> lines, String key, List<String> ids) {
        if (!ids.isEmpty()) lines.add(Component.translatable("jei.cropgenetics.conditions." + key,
                String.join(", ", ids)));
    }

    private static void addChemistry(List<Component> lines, String key,
                                     java.util.Optional<SoilChemistryRule.Condition> condition) {
        condition.ifPresent(rule -> lines.add(Component.translatable("jei.cropgenetics.conditions." + key,
                rule.category(), rule.comparison().name(), rule.toleranceFactor())));
    }
}

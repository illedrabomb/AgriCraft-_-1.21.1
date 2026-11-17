package dev.illedrabomb.cropgenetics.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import dev.illedrabomb.cropgenetics.content.CropConditions;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.content.GourdFruits;
import dev.illedrabomb.cropgenetics.content.GourdFruitRules;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Read-only administrative datapack diagnostics for players with permission
 * level 2 and for the dedicated server console. Never initiates a reload.
 */
public final class CropDataCommand {
    private static final int PAGE_SIZE = 8;

    private CropDataCommand() {}

    public static void register(RegisterCommandsEvent event, AgriPlantsRegistry registry) {
        event.getDispatcher().register(Commands.literal("cropgenetics")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("data")
                        .executes(ctx -> summary(ctx.getSource(), registry))
                        .then(Commands.literal("summary")
                                .executes(ctx -> summary(ctx.getSource(), registry)))
                        .then(Commands.literal("errors")
                                .executes(ctx -> list(ctx.getSource(), registry, false, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> list(ctx.getSource(), registry, false,
                                                IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("all")
                                .executes(ctx -> list(ctx.getSource(), registry, true, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> list(ctx.getSource(), registry, true,
                                                IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("pam")
                                .executes(ctx -> pamSummary(ctx.getSource(), registry)))
                        .then(Commands.literal("gourd")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> gourd(ctx.getSource(), registry,
                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("tags")
                                .executes(ctx -> tags(ctx.getSource(), registry, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> tags(ctx.getSource(), registry,
                                                IntegerArgumentType.getInteger(ctx, "page")))))));
    }

    private static int summary(CommandSourceStack source, AgriPlantsRegistry registry) {
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        CropReloadReport report = snapshot.report();
        if (notLoaded(snapshot)) {
            tell(source, "Crop Genetics: no datapack reload report available yet.");
            return 0;
        }

        tell(source, "Crop Genetics datapack: " + snapshot.plants().size() + " plants, "
                + snapshot.mutations().size() + " mutations, "
                + report.entries().size() + " resource files.");
        CropReloadReport.PamDiscoveryStats pam = report.pamDiscoveryStats();
        tell(source, "  Pam's auto crops: " + pam.autoDiscovered()
                + ", explicit plant files loaded (all mods): " + pam.explicitLoaded()
                + " - details: /cropgenetics data pam");
        Map<CropReloadReport.Status, Integer> totals = report.totals();
        for (CropReloadReport.Status status : CropReloadReport.Status.values()) {
            int count = totals.getOrDefault(status, 0);
            if (count != 0) tell(source, "  " + status + ": " + count);
        }
        if (!snapshot.ambiguousSeedItems().isEmpty())
            tell(source, "  Ambiguous vanilla seed IDs: "
                    + snapshot.ambiguousSeedItems().size());
        tell(source, "Use /cropgenetics data errors [page], all [page], or tags [page].");
        return report.entries().size();
    }

    /** Read-only breakdown from the same immutable snapshot as /data. */
    private static int pamSummary(CommandSourceStack source, AgriPlantsRegistry registry) {
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        if (notLoaded(snapshot)) {
            tell(source, "Crop Genetics: no datapack reload report available yet.");
            return 0;
        }
        CropReloadReport.PamDiscoveryStats stats = snapshot.report().pamDiscoveryStats();
        tell(source, "Pam's HarvestCraft 2 Crops - last server reload:");
        tell(source, "  Explicit plants loaded: " + stats.explicitLoaded());
        tell(source, "  Auto-discovered species: " + stats.autoDiscovered());
        tell(source, "  Auto candidates in conflict: " + stats.autoConflicts());
        tell(source, "  Configured pairs missing registered items: " + stats.autoMissingItems());
        tell(source, "  Other auto diagnostics: " + stats.autoWarnings());
        tell(source, "  Auto-discovery suspended by invalid data/config: "
                + (stats.suspended() ? "yes" : "no"));
        tell(source, "  Total active species (all mods): " + snapshot.plants().size());
        tell(source, "Details: /cropgenetics data errors [page] or all [page].");
        return stats.autoDiscovered();
    }

    /**
     * Diagnose fruit chance, growth conditions, and all four placement sites
     * without rolling randomness or modifying the world.
     */
    private static int gourd(CommandSourceStack source, AgriPlantsRegistry registry,
                             BlockPos pos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = source.getLevel();
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) {
            tell(source, "No planted Crop Sticks block entity at " + pos.toShortString());
            return 0;
        }
        Genome genome = crop.genome();
        if (genome == null) {
            tell(source, "Crop Sticks at " + pos.toShortString() + " are empty.");
            return 0;
        }
        PlantDefinition plant = registry.snapshot().plants().get(genome.plantId());
        if (plant == null) {
            tell(source, "Species missing from active datapack: " + genome.plantId());
            return 0;
        }
        double factor = CropConditions.growthFactor(level, pos, plant, genome);
        double chance = GourdFruitRules.chance(plant.fruitChance(),
                genome.gain().expressed(
                        dev.illedrabomb.cropgenetics.genetics.GenePair.TraitMode.MAX),
                factor);
        tell(source, "Gourd diagnostic at " + pos.toShortString()
                + " (read-only, current loaded chunks):");
        int randomTickSpeed = level.getGameRules().getInt(
                net.minecraft.world.level.GameRules.RULE_RANDOMTICKING);
        boolean ticking = crop.getBlockState().isRandomlyTicking();
        tell(source, "  growthFactor=" + factor + ", fruitChancePerRandomTick="
                + chance
                + ", brightness=" + level.getRawBrightness(pos, 0));
        tell(source, "  randomTickSpeed=" + randomTickSpeed
                + ", blockRandomlyTicks=" + ticking
                + ", activeFruitAttemptPossible="
                + (factor > 0D && randomTickSpeed > 0 && ticking
                    && crop.isMature(plant) && !crop.fruitReady()
                    && plant.harvestMode() == dev.illedrabomb.cropgenetics.data.HarvestMode.GOURD));
        tell(source, "  matureFruitTicks=" + crop.matureFruitTicks()
                + ", matureTicksBlockedByEnvironment=" + crop.matureFruitTicksBlocked());
        tell(source, "  fruitRolls=" + crop.fruitChanceRolls()
                + ", rollsPassed=" + crop.fruitChancePassed()
                + ", placementsSucceeded=" + crop.fruitPlacementSucceeded()
                + ", placementFailuresAfterPassedRoll="
                + Math.max(0L, crop.fruitChancePassed() - crop.fruitPlacementSucceeded()));
        int strength = genome.strength().expressed(
                dev.illedrabomb.cropgenetics.genetics.GenePair.TraitMode.MAX);
        var requirements = plant.requirements();
        tell(source, "  conditions: plantingSite="
                + CropConditions.matchesPlantingSite(level, pos, plant)
                + ", location=" + CropConditions.matchesLocation(level, pos, requirements)
                + ", environment=" + CropConditions.matchesEnvironment(
                        level, pos, requirements.environment())
                + ", season=" + CropConditions.matchesSeason(
                        level, pos, requirements.seasonRule().allowedSeasons())
                + ", chemistry=" + CropConditions.matchesChemistry(
                        level, pos, requirements.soilChemistry(), strength));
        tell(source, "  light preferred=" + requirements.minLight() + ".."
                + requirements.maxLight() + ", tolerance=" + requirements.lightTolerance()
                + ", soilGrowthModifier="
                + dev.illedrabomb.cropgenetics.CropGeneticsMod.soilProfiles()
                        .growthModifier(level.getBlockState(pos.below())));
        GourdFruits.placementDiagnostics(level, pos, plant, crop)
                .forEach(line -> tell(source, "  " + line));
        return 1;
    }

    private static int list(CommandSourceStack source, AgriPlantsRegistry registry,
                            boolean includeLoaded, int page) {
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        if (notLoaded(snapshot)) {
            tell(source, "Crop Genetics: no datapack reload report available yet.");
            return 0;
        }

        List<CropReloadReport.Entry> entries = snapshot.report().entries().stream()
                .filter(entry -> includeLoaded
                        || (entry.status() != CropReloadReport.Status.LOADED
                            && entry.status() != CropReloadReport.Status.AUTO_DISCOVERED))
                .sorted(Comparator.comparing((CropReloadReport.Entry e) -> e.kind().name())
                        .thenComparing(e -> e.source().toString()))
                .toList();

        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) {
            tell(source, "Page " + page + " unavailable; valid pages: 1-" + pages + ".");
            return 0;
        }
        tell(source, "Crop Genetics " + (includeLoaded ? "all entries" : "issues")
                + " - page " + page + "/" + pages + " (" + entries.size() + " entries):");
        // Bounds checked before multiplication: a huge page argument must not overflow.
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(entries.size(), from + PAGE_SIZE);
        if (entries.isEmpty()) tell(source, "No matching entries.");
        for (int i = from; i < to; i++) {
            CropReloadReport.Entry entry = entries.get(i);
            String description = entry.detail().isBlank() ? "" : " - " + entry.detail();
            // Some JSON error strings contain excessive text. Limit chat
            // output per entry; full details remain in the server logs.
            if (description.length() > 180)
                description = description.substring(0, 177) + "...";
            tell(source, "[" + entry.status() + "] " + entry.kind() + " "
                    + entry.source() + description);
        }
        return to - from;
    }

    /**
     * Query the live bound BLOCK registry tags, not the datapack file list.
     * A tag may be provided through built-in or generated data sources.
     */
    private static int tags(CommandSourceStack source, AgriPlantsRegistry registry, int page) {
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        if (notLoaded(snapshot)) {
            tell(source, "Crop Genetics: no datapack reload report available yet.");
            return 0;
        }

        List<String> lines = new ArrayList<>();
        snapshot.plants().values().stream()
                .sorted(Comparator.comparing(PlantDefinition::id))
                .forEach(plant -> plant.requirements().soilTag().ifPresent(value -> {
                    ResourceLocation id = ResourceLocation.tryParse(value);
                    if (id == null) {
                        lines.add(plant.id() + ": INVALID tag ID " + value);
                        return;
                    }
                    TagKey<Block> key = TagKey.create(Registries.BLOCK, id);
                    var holders = BuiltInRegistries.BLOCK.getTag(key);
                    if (holders.isEmpty()) {
                        lines.add(plant.id() + ": UNBOUND #" + value);
                    } else if (holders.get().size() == 0) {
                        lines.add(plant.id() + ": EMPTY #" + value);
                    } else {
                        lines.add(plant.id() + ": #" + value + " (" + holders.get().size()
                                + " registered blocks)");
                    }
                }));

        int pages = Math.max(1, (lines.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) {
            tell(source, "Page " + page + " unavailable; valid pages: 1-" + pages + ".");
            return 0;
        }
        tell(source, "Runtime soil tags - page " + page + "/" + pages
                + " (" + lines.size() + " crop references):");
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(lines.size(), from + PAGE_SIZE);
        if (lines.isEmpty()) tell(source, "No active crops require a soil tag.");
        for (int i = from; i < to; i++) tell(source, lines.get(i));
        return to - from;
    }

    private static boolean notLoaded(AgriPlantsRegistry.Snapshot snapshot) {
        return snapshot.report().entries().isEmpty()
                && snapshot.plants().isEmpty() && snapshot.mutations().isEmpty();
    }

    private static void tell(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal(text), false);
    }
}

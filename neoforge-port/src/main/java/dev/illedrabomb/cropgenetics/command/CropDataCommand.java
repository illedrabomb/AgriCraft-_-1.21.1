package dev.illedrabomb.cropgenetics.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import dev.illedrabomb.cropgenetics.content.CropConditions;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.content.GourdFruits;
import dev.illedrabomb.cropgenetics.content.GourdFruitRules;
import dev.illedrabomb.cropgenetics.config.HarvestDropOverride;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Administrative datapack diagnostics plus a separate, temporary harvest
 * multiplier override and operator-only test seed fixtures.
 * Requires permission level 2; test seeds require a player inventory.
 */
public final class CropDataCommand {
    private static final int PAGE_SIZE = 8;

    private CropDataCommand() {}

    public static void register(RegisterCommandsEvent event, AgriPlantsRegistry registry) {
        var data = Commands.literal("data")
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
                .then(Commands.literal("gourd")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> gourd(ctx.getSource(), registry,
                                        BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                .then(Commands.literal("tags")
                        .executes(ctx -> tags(ctx.getSource(), registry, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> tags(ctx.getSource(), registry,
                                        IntegerArgumentType.getInteger(ctx, "page")))));

        for (CropDataCommandExtensions.Extension addon : CropDataCommandExtensions.registered()) {
            data.then(Commands.literal(addon.name())
                    .executes(ctx -> addon.handler().execute(ctx.getSource(), registry)));
        }
        event.getDispatcher().register(Commands.literal("cropgenetics")
                .requires(source -> source.hasPermission(2))
                .then(data)
                .then(Commands.literal("testseed")
                        .then(Commands.argument("species", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        registry.snapshot().plants().keySet(), builder))
                                // One number is the legacy Gain-only shortcut; three
                                // numbers follow the visible Growth/Gain/Strength order.
                                .then(Commands.argument("gainOrGrowth", IntegerArgumentType.integer(1, 10))
                                        .executes(ctx -> testSeed(ctx.getSource(), registry,
                                                TestSeedGenomes.gainOnly(
                                                        ResourceLocationArgument.getId(ctx, "species").toString(),
                                                        IntegerArgumentType.getInteger(ctx, "gainOrGrowth"))))
                                        .then(Commands.argument("gain", IntegerArgumentType.integer(1, 10))
                                                .then(Commands.argument("strength", IntegerArgumentType.integer(1, 10))
                                                        .executes(ctx -> testSeed(ctx.getSource(), registry,
                                                                TestSeedGenomes.full(
                                                                        ResourceLocationArgument.getId(ctx, "species").toString(),
                                                                        IntegerArgumentType.getInteger(ctx, "gainOrGrowth"),
                                                                        IntegerArgumentType.getInteger(ctx, "gain"),
                                                                        IntegerArgumentType.getInteger(ctx, "strength")))))))))
                .then(Commands.literal("harvestmultiplier")
                        .executes(ctx -> showHarvestMultiplier(ctx.getSource()))
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetHarvestMultiplier(ctx.getSource())))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0D, 64.0D))
                                .executes(ctx -> setHarvestMultiplier(ctx.getSource(),
                                        DoubleArgumentType.getDouble(ctx, "value"))))));
    }

    /** Supply an unanalysed genetic seed fixture to the invoking operator only. */
    private static int testSeed(CommandSourceStack source, AgriPlantsRegistry registry,
                                Genome genome)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        String speciesId = genome.plantId();
        if (ResourceLocation.tryParse(speciesId) == null
                || !registry.snapshot().plants().containsKey(speciesId)) {
            source.sendFailure(Component.translatable(
                    "command.cropgenetics.testseed.unknown_species", speciesId));
            return 0;
        }
        // Refuse a full inventory instead of dropping test items into the world.
        if (player.getInventory().getFreeSlot() < 0) {
            source.sendFailure(Component.translatable("command.cropgenetics.testseed.inventory_full"));
            return 0;
        }
        ItemStack seed = CropItems.seedWithGenome(genome);
        if (!player.getInventory().add(seed)) {
            source.sendFailure(Component.translatable("command.cropgenetics.testseed.insert_failed"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.cropgenetics.testseed.received",
                speciesId, genome.growth(), genome.gain(), genome.strength()), false);
        return 1;
    }

    private static int showHarvestMultiplier(CommandSourceStack source) {
        var server = source.getServer();
        var origin = Component.translatable(HarvestDropOverride.isOverridden(server)
                ? "command.cropgenetics.harvestmultiplier.temporary"
                : "command.cropgenetics.harvestmultiplier.config");
        source.sendSuccess(() -> Component.translatable(
                "command.cropgenetics.harvestmultiplier.show",
                HarvestDropOverride.effective(server), origin), false);
        return 1;
    }

    private static int setHarvestMultiplier(CommandSourceStack source, double value) {
        HarvestDropOverride.set(source.getServer(), value);
        source.sendSuccess(() -> Component.translatable(
                "command.cropgenetics.harvestmultiplier.set", value), false);
        return 1;
    }

    private static int resetHarvestMultiplier(CommandSourceStack source) {
        HarvestDropOverride.reset(source.getServer());
        return showHarvestMultiplier(source);
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
        tell(source, "  Addon auto-discovered species: "
                + report.count(CropReloadReport.Kind.PLANT,
                        CropReloadReport.Status.AUTO_DISCOVERED));
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
                genome.gain(),
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
        tell(source, "  noEligibleTarget=" + crop.fruitPlacementNoEligibleTarget()
                + ", eligibleTargetButPlacementFailed=" + crop.fruitPlacementEligibleFailed());
        int strength = genome.strength();
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

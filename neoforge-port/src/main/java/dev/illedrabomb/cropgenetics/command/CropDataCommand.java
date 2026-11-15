package dev.illedrabomb.cropgenetics.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
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
        Map<CropReloadReport.Status, Integer> totals = report.totals();
        for (CropReloadReport.Status status : CropReloadReport.Status.values()) {
            int count = totals.getOrDefault(status, 0);
            if (count != 0) tell(source, "  " + status + ": " + count);
        }
        if (!snapshot.ambiguousSeedItems().isEmpty())
            tell(source, "  Ambiguous vanilla seed IDs: "
                    + snapshot.ambiguousSeedItems().size());
        tell(source, "Use /cropgenetics data errors [page] or /cropgenetics data all [page].");
        return report.entries().size();
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
                        || entry.status() != CropReloadReport.Status.LOADED)
                .sorted(Comparator.comparing((CropReloadReport.Entry e) -> e.kind().name())
                        .thenComparing(e -> e.source().toString()))
                .toList();

        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) {
            tell(source, "Page " + page + " unavailable; valid pages: 1-" + pages + ".");
            return 0;
        }
        tell(source, "Crop Genetics " + (includeLoaded ? "all files" : "issues")
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

    private static boolean notLoaded(AgriPlantsRegistry.Snapshot snapshot) {
        return snapshot.report().entries().isEmpty()
                && snapshot.plants().isEmpty() && snapshot.mutations().isEmpty();
    }

    private static void tell(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal(text), false);
    }
}

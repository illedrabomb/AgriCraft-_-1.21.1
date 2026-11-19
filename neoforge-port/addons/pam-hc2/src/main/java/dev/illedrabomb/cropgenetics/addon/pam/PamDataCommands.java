package dev.illedrabomb.cropgenetics.addon.pam;

import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/** Diagnostics for this installed addon only. */
public final class PamDataCommands {
    private PamDataCommands() {}

    /** Read-only breakdown from the same immutable snapshot as /data. */
    public static int show(CommandSourceStack source, AgriPlantsRegistry registry) {
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

    private static boolean notLoaded(AgriPlantsRegistry.Snapshot snapshot) {
        return snapshot.report().entries().isEmpty()
                && snapshot.plants().isEmpty() && snapshot.mutations().isEmpty();
    }

    private static void tell(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal(text), false);
    }
}

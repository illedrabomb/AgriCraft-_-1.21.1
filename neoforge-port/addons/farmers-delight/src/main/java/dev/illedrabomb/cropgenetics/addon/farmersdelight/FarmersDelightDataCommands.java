package dev.illedrabomb.cropgenetics.addon.farmersdelight;

import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/** Diagnostics for this installed addon only. */
public final class FarmersDelightDataCommands {
    private FarmersDelightDataCommands() {}

    /** Read-only Farmer's Delight adapter status for support and modpack testers. */
    public static int show(CommandSourceStack source,
                                             AgriPlantsRegistry registry) {
        AgriPlantsRegistry.Snapshot snapshot = registry.snapshot();
        if (notLoaded(snapshot)) {
            tell(source, "Crop Genetics: no datapack reload report available yet.");
            return 0;
        }
        CropReloadReport.AdapterDiscoveryStats stats =
                snapshot.report().farmersDelightStats();
        tell(source, "Farmer's Delight compatibility - last server reload:");
        tell(source, "  Auto-discovered pilot species: " + stats.discovered());
        tell(source, "  Conflicting candidates: " + stats.conflicts());
        tell(source, "  Configured pairs with missing items: " + stats.missingItems());
        tell(source, "  Other adapter diagnostics: " + stats.otherWarnings());
        tell(source, "  Auto-discovery suspended: " + (stats.suspended() ? "yes" : "no"));
        tell(source, "  Scope: cabbage, tomato, onion; rice not yet supported.");
        tell(source, "Details: /cropgenetics data errors [page] or all [page].");
        return stats.discovered();
    }

    private static boolean notLoaded(AgriPlantsRegistry.Snapshot snapshot) {
        return snapshot.report().entries().isEmpty()
                && snapshot.plants().isEmpty() && snapshot.mutations().isEmpty();
    }

    private static void tell(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal(text), false);
    }
}

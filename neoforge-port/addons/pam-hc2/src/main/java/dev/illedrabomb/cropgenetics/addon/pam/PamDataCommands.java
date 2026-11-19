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
        CropReloadReport.AutoDiscoveryStats stats = snapshot.report()
                .autoDiscoveryStats("auto_pam/", "cropgenetics/pam_auto_discovery.json");
        long explicitLoaded = snapshot.report().entries().stream()
                .filter(entry -> entry.kind() == CropReloadReport.Kind.PLANT
                        && entry.source().getPath().startsWith("cropgenetics/plants/")
                        && (entry.status() == CropReloadReport.Status.LOADED
                            || entry.status() == CropReloadReport.Status.WARNING))
                .count();
        tell(source, "Pam's HarvestCraft 2 Crops - last server reload:");
        tell(source, "  Explicit plants loaded: " + explicitLoaded);
        tell(source, "  Auto-discovered species: " + stats.discovered());
        tell(source, "  Auto candidates in conflict: " + stats.conflicts());
        tell(source, "  Configured pairs missing registered items: " + stats.missingItems());
        tell(source, "  Other auto diagnostics: " + stats.otherWarnings());
        tell(source, "  Auto-discovery suspended by invalid data/config: "
                + (stats.suspended() ? "yes" : "no"));
        tell(source, "  Total active species (all mods): " + snapshot.plants().size());
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

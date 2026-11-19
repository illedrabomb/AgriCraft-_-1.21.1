package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropOptionalDiscoveryStatsTest {
    private static CropReloadReport.Entry entry(String path,
            CropReloadReport.Status status) {
        return new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                ResourceLocation.fromNamespaceAndPath("cropgenetics", path),
                "test:crop_plant", status, "test");
    }

    @Test
    void pamIntentionalOverrideIsNotAnActualWarning() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_pam/overridden.json", CropReloadReport.Status.DISABLED),
                entry("auto_pam/missing.json", CropReloadReport.Status.AUTO_MISSING_ITEM),
                entry("auto_pam/valid.json", CropReloadReport.Status.AUTO_DISCOVERED),
                entry("auto_pam/broken.json", CropReloadReport.Status.WARNING),
                entry("auto_pam/conflict.json", CropReloadReport.Status.CONFLICT)));
        var stats = report.pamDiscoveryStats();
        assertEquals(1, stats.autoDiscovered());
        assertEquals(1, stats.autoConflicts());
        assertEquals(1, stats.autoMissingItems());
        assertEquals(1, stats.autoWarnings());
    }

    @Test
    void farmerIntentionalOverrideDoesNotInflateWarnings() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_farmers_delight/onion.json", CropReloadReport.Status.DISABLED),
                entry("auto_farmers_delight/cabbage.json", CropReloadReport.Status.AUTO_DISCOVERED),
                entry("auto_farmers_delight/tomato.json", CropReloadReport.Status.AUTO_MISSING_ITEM),
                entry("auto_farmers_delight/bad.json", CropReloadReport.Status.WARNING),
                entry("auto_farmers_delight/conflict.json", CropReloadReport.Status.CONFLICT)));
        var stats = report.farmersDelightStats();
        assertEquals(1, stats.discovered());
        assertEquals(1, stats.conflicts());
        assertEquals(1, stats.missingItems());
        assertEquals(1, stats.otherWarnings());
    }

    @Test
    void plainDisabledEntriesStayVisibleInTheFullReport() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_pam/override.json", CropReloadReport.Status.DISABLED)));
        assertEquals(1, report.count(CropReloadReport.Kind.PLANT,
                CropReloadReport.Status.DISABLED));
        assertEquals(0, report.pamDiscoveryStats().autoWarnings());
    }
}

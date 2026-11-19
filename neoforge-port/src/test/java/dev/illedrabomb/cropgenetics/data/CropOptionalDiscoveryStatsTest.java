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
                "test:crop_plant", status, "");
    }

    @Test
    void intentionalExplicitOverrideIsNotReportedAsWarning() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_example/overridden.json", CropReloadReport.Status.DISABLED),
                entry("auto_example/missing.json", CropReloadReport.Status.AUTO_MISSING_ITEM),
                entry("auto_example/valid.json", CropReloadReport.Status.AUTO_DISCOVERED),
                entry("auto_example/broken.json", CropReloadReport.Status.WARNING),
                entry("auto_example/conflict.json", CropReloadReport.Status.CONFLICT),
                entry("auto_other/unrelated.json", CropReloadReport.Status.AUTO_DISCOVERED)));
        var stats = report.autoDiscoveryStats("auto_example/", "");
        assertEquals(1, stats.discovered());
        assertEquals(1, stats.conflicts());
        assertEquals(1, stats.missingItems());
        assertEquals(1, stats.otherWarnings());
        assertFalse(stats.suspended());
    }

    @Test
    void sentinelAndInvalidPolicyMarkAddonAsSuspended() {
        var sentinel = new CropReloadReport(List.of(
                entry("auto_example/discovery_suspended", CropReloadReport.Status.WARNING)));
        assertTrue(sentinel.autoDiscoveryStats("auto_example/", "").suspended());

        var invalidPolicy = new CropReloadReport(List.of(
                entry("cropgenetics/example_policy.json", CropReloadReport.Status.INVALID)));
        assertTrue(invalidPolicy.autoDiscoveryStats(
                "auto_example/", "cropgenetics/example_policy.json").suspended());
    }

    @Test
    void disabledEntriesRemainInFullReport() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_example/override.json", CropReloadReport.Status.DISABLED)));
        assertEquals(1, report.count(CropReloadReport.Kind.PLANT,
                CropReloadReport.Status.DISABLED));
        assertEquals(0, report.autoDiscoveryStats("auto_example/", "").otherWarnings());
    }

    @Test
    void invalidNamespacePrefixIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> CropReloadReport.empty().autoDiscoveryStats("../escape/", ""));
    }
}

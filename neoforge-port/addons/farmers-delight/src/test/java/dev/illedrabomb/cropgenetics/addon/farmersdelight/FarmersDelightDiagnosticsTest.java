package dev.illedrabomb.cropgenetics.addon.farmersdelight;

import dev.illedrabomb.cropgenetics.data.*;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmersDelightDiagnosticsTest {
    private static CropReloadReport.Entry entry(String path, CropReloadReport.Status status) {
        return new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                ResourceLocation.fromNamespaceAndPath("cropgenetics", path), "", status, "");
    }

    @Test
    void countsOnlyFarmersDelightPilotEntries() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_farmers_delight/cabbage.json", CropReloadReport.Status.AUTO_DISCOVERED),
                entry("auto_farmers_delight/tomato.json", CropReloadReport.Status.AUTO_DISCOVERED),
                entry("auto_farmers_delight/onion.json", CropReloadReport.Status.CONFLICT),
                entry("auto_farmers_delight/unknown.json", CropReloadReport.Status.AUTO_MISSING_ITEM),
                entry("auto_farmers_delight/something_else", CropReloadReport.Status.WARNING),
                entry("auto_pam/mustardseeds.json", CropReloadReport.Status.AUTO_DISCOVERED),
                entry("cropgenetics/plants/other.json", CropReloadReport.Status.LOADED)
        ));

        CropReloadReport.AutoDiscoveryStats farmers = report.autoDiscoveryStats("auto_farmers_delight/", "");
        assertEquals(2, farmers.discovered());
        assertEquals(1, farmers.conflicts());
        assertEquals(1, farmers.missingItems());
        assertEquals(1, farmers.otherWarnings());
        assertFalse(farmers.suspended());
        assertEquals(1, report.autoDiscoveryStats("auto_pam/", "").discovered());
    }

    @Test
    void explicitlyTracksSuspensionWithoutCountingItAsAPlant() {
        CropReloadReport report = new CropReloadReport(List.of(
                entry("auto_farmers_delight/discovery_suspended", CropReloadReport.Status.INVALID)
        ));

        CropReloadReport.AutoDiscoveryStats farmers = report.autoDiscoveryStats("auto_farmers_delight/", "");
        assertTrue(farmers.suspended());
        assertEquals(0, farmers.discovered());
        assertEquals(0, farmers.conflicts());
    }

    @Test
    void noOptionalModEntriesProducesEmptyStatistics() {
        CropReloadReport.AutoDiscoveryStats farmers = CropReloadReport.empty().autoDiscoveryStats("auto_farmers_delight/", "");
        assertEquals(0, farmers.discovered());
        assertEquals(0, farmers.conflicts());
        assertEquals(0, farmers.missingItems());
        assertEquals(0, farmers.otherWarnings());
        assertFalse(farmers.suspended());
    }
}

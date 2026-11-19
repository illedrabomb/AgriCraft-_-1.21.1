package dev.illedrabomb.cropgenetics.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CropReloadLogSeverityTest {
    @Test
    void expectedSkipsAreInformational() {
        for (CropReloadReport.Status status : new CropReloadReport.Status[] {
                CropReloadReport.Status.DISABLED,
                CropReloadReport.Status.MISSING_MOD
        }) {
            assertFalse(CropReloadLogSeverity.shouldWarn(status));
            assertTrue(CropReloadLogSeverity.shouldLogInfo(status));
        }
    }

    @Test
    void validEntriesNeedNoIndividualLog() {
        for (CropReloadReport.Status status : new CropReloadReport.Status[] {
                CropReloadReport.Status.LOADED,
                CropReloadReport.Status.AUTO_DISCOVERED
        }) {
            assertFalse(CropReloadLogSeverity.shouldWarn(status));
            assertFalse(CropReloadLogSeverity.shouldLogInfo(status));
        }
    }

    @Test
    void ActualProblemsRemainWarnings() {
        for (CropReloadReport.Status status : new CropReloadReport.Status[] {
                CropReloadReport.Status.INVALID,
                CropReloadReport.Status.CONFLICT,
                CropReloadReport.Status.MISSING_SPECIES,
                CropReloadReport.Status.AUTO_MISSING_ITEM,
                CropReloadReport.Status.WARNING
        }) {
            assertTrue(CropReloadLogSeverity.shouldWarn(status));
            assertFalse(CropReloadLogSeverity.shouldLogInfo(status));
        }
    }
}

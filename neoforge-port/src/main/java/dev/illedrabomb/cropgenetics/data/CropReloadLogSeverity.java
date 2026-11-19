package dev.illedrabomb.cropgenetics.data;

/** Log severity does not change the immutable reload report or crop snapshot. */
final class CropReloadLogSeverity {
    private CropReloadLogSeverity() {}

    static boolean shouldWarn(CropReloadReport.Status status) {
        return switch (status) {
            case INVALID, CONFLICT, MISSING_SPECIES, AUTO_MISSING_ITEM, WARNING -> true;
            case LOADED, AUTO_DISCOVERED, DISABLED, MISSING_MOD -> false;
        };
    }

    static boolean shouldLogInfo(CropReloadReport.Status status) {
        return status == CropReloadReport.Status.DISABLED
                || status == CropReloadReport.Status.MISSING_MOD;
    }
}

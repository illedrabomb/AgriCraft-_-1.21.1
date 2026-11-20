package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.HarvestMode;

/** Reload policy for an internal gourd fruit after harvest-mode changes. */
final class CropFruitTransitionRules {
    private CropFruitTransitionRules() {}

    static boolean staleInternalFruit(HarvestMode activeMode, boolean ready,
                                      boolean hasDirection, boolean hasToken) {
        return activeMode == HarvestMode.STANDARD && ready && !hasDirection && !hasToken;
    }

    static boolean canHarvestStandard(HarvestMode activeMode, boolean mature,
                                       boolean ready, boolean hasDirection, boolean hasToken) {
        return activeMode == HarvestMode.STANDARD && mature
                && ((!ready && !hasDirection && !hasToken)
                    || staleInternalFruit(activeMode, ready, hasDirection, hasToken));
    }

    static boolean canHarvestInternalGourd(HarvestMode activeMode, boolean mature,
                                           boolean ready, boolean hasDirection, boolean hasToken) {
        return activeMode == HarvestMode.GOURD && mature && ready
                && !hasDirection && !hasToken;
    }
}

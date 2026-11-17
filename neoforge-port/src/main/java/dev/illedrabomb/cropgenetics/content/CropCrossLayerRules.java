package dev.illedrabomb.cropgenetics.content;

/** Pure state transition rule for single planting sticks and crossed breeding sticks. */
final class CropCrossLayerRules {
    private CropCrossLayerRules() {}

    /** Empty crossed sticks stay crossed; a newly planted offspring consumes the upper layer. */
    static boolean keepCrossLayer(boolean wasCrossed, boolean planted) {
        return wasCrossed && !planted;
    }
}

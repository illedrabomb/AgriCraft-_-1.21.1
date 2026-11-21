package dev.illedrabomb.cropgenetics.content;

/** Final per-product multiplier, with no way to bypass the one-stack cap. */
public final class HarvestDropScaling {
    private HarvestDropScaling() {}

    public static int count(int base, double multiplier, int itemStackLimit) {
        if (base < 0 || itemStackLimit < 1 || !Double.isFinite(multiplier) || multiplier < 0)
            throw new IllegalArgumentException("Invalid harvest drop scaling input");
        long scaled = Math.round(base * multiplier);
        return (int) Math.min(Math.min(itemStackLimit, 64), Math.max(0L, scaled));
    }
}

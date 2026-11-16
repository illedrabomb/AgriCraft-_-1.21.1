package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;

import javax.annotation.Nullable;

/**
 * One disclosure policy for every supported presentation surface.
 *
 * A plant's visible identity is not confidential; numerical gene pairs are
 * confidential until the individual seed or planted crop has been analyzed.
 * Optional overlay/recipe integrations must call this policy rather than
 * showing a BlockEntity's or ItemStack's raw genome.
 *
 * NOTE: This is a presentation rule, NOT security against third-party mods
 * that independently inspect Minecraft's synchronized data components/NBT.
 */
public final class GeneticDisclosure {
    private GeneticDisclosure() {}

    public static boolean mayShowGenes(@Nullable Genome genome, boolean analyzed) {
        return genome != null && analyzed;
    }
}

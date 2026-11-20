package dev.illedrabomb.cropgenetics.genetics;

import java.util.List;
import java.util.Objects;

/** Prevents a different species from transferring an advanced genome in one cross. */
public final class SpeciesGeneLimits {
    private SpeciesGeneLimits() {}

    /**
     * Applied after inheritance/mutation and after the offspring species is chosen.
     * Ordinary same-species breeding has no additional cap. A novel species is
     * capped at four, while a participating parent species keeps at least its
     * own best existing value or five, whichever is higher, independently per stat.
     * This is only a ceiling: weak children remain weak and excess is discarded.
     */
    public static Genome apply(Genome child, List<Genome> parents) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(parents);
        if (parents.size() < 2 || parents.size() > 4 || parents.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Requires 2 to 4 non-null parents");

        String species = parents.getFirst().plantId();
        // Same-species breeding is unrestricted ONLY if the child also has
        // that species. A datapack mutation may produce a novel species even
        // from identical-species parents; that child must still cap at four.
        if (child.plantId().equals(species)
                && parents.stream().allMatch(parent -> species.equals(parent.plantId()))) return child;

        boolean hasSameSpeciesParent = parents.stream()
                .anyMatch(parent -> child.plantId().equals(parent.plantId()));
        if (!hasSameSpeciesParent) {
            return new Genome(child.plantId(), Math.min(4, child.growth()),
                    Math.min(4, child.gain()), Math.min(4, child.strength()));
        }

        int growthCap = 5;
        int gainCap = 5;
        int strengthCap = 5;
        for (Genome parent : parents) {
            if (!child.plantId().equals(parent.plantId())) continue;
            growthCap = Math.max(growthCap, parent.growth());
            gainCap = Math.max(gainCap, parent.gain());
            strengthCap = Math.max(strengthCap, parent.strength());
        }
        return new Genome(child.plantId(), Math.min(growthCap, child.growth()),
                Math.min(gainCap, child.gain()), Math.min(strengthCap, child.strength()));
    }
}

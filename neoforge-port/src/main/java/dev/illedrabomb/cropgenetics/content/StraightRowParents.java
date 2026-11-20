package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

/** Bounded, contiguous row sampling without loading distant chunks. */
final class StraightRowParents {
    private StraightRowParents() {}

    record Selection(List<Genome> geneticParents, int length) {
        Selection {
            geneticParents = List.copyOf(geneticParents);
        }
    }

    /**
     * The first parent at distance 1 has already been validated by the caller.
     * The lookup must return null for gaps, immature crops and unloaded chunks.
     * Parents beyond the fourth only affect row length, never inheritance.
     */
    static Selection scan(Genome first, IntFunction<Genome> parentAtDistance) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(parentAtDistance);
        List<Genome> geneticParents = new ArrayList<>(4);
        geneticParents.add(first);
        int length = 1;
        for (int distance = 2; distance <= CrossFertilityRules.MAX_ROW_LENGTH; distance++) {
            Genome parent = parentAtDistance.apply(distance);
            if (parent == null) break;
            length++;
            if (geneticParents.size() < 4) geneticParents.add(parent);
        }
        return new Selection(geneticParents, length);
    }
}

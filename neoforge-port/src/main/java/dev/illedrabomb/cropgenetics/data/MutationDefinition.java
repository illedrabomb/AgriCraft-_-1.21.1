package dev.illedrabomb.cropgenetics.data;

import java.util.Objects;

/** Species mutation definition, independent from the numeric-gene mutator. */
public record MutationDefinition(String childPlantId, String parentA, String parentB, double chance) {
    public MutationDefinition {
        Objects.requireNonNull(childPlantId);
        Objects.requireNonNull(parentA);
        Objects.requireNonNull(parentB);
        if (chance < 0 || chance > 1 || !Double.isFinite(chance)) {
            throw new IllegalArgumentException("Mutation chance must be in [0, 1]");
        }
    }

    public boolean matchesParents(String a, String b) {
        return (parentA.equals(a) && parentB.equals(b)) || (parentA.equals(b) && parentB.equals(a));
    }
}

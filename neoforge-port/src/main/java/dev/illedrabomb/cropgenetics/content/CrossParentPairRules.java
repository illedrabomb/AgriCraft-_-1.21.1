package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import java.util.List;
import java.util.function.BiPredicate;

/** A mutation recipe can match any two distinct contributing parent plants. */
final class CrossParentPairRules {
    private CrossParentPairRules() {}

    static boolean matchesAnyPair(List<Genome> parents,
                                  BiPredicate<String, String> matches) {
        for (int i = 0; i < parents.size(); i++) {
            for (int j = i + 1; j < parents.size(); j++) {
                if (matches.test(parents.get(i).plantId(), parents.get(j).plantId())) {
                    return true;
                }
            }
        }
        return false;
    }
}

package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.MutationDefinition;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Pure, deterministic discovery filter. Neither JEI nor the client-side
 * journal guesses mutation recipes from un-synchronized datapacks.
 */
final class JournalMutationRules {
    private JournalMutationRules() {}

    static List<String> revealedTriples(Set<String> discovered,
                                        Collection<MutationDefinition> activeMutations) {
        return activeMutations.stream()
                .filter(def -> discovered.contains(def.parentA())
                        && discovered.contains(def.parentB())
                        && discovered.contains(def.childPlantId()))
                .map(def -> def.parentA() + ">" + def.parentB() + ">" + def.childPlantId())
                .distinct()
                .sorted()
                .toList();
    }
}

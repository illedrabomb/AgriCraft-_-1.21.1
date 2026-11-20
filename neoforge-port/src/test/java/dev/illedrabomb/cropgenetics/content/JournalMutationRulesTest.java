package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.MutationDefinition;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** A journal must never reveal species that have not been analyzed. */
class JournalMutationRulesTest {
    private static final String WHEAT = "minecraft:wheat_plant";
    private static final String CARROT = "minecraft:carrot_plant";
    private static final String POTATO = "minecraft:potato_plant";
    private static final String BEET = "minecraft:beet_root_plant";

    @Test
    void unknownChildOrParentNeverAppears() {
        MutationDefinition known = new MutationDefinition(POTATO, WHEAT, CARROT, 0.1D);
        MutationDefinition secret = new MutationDefinition(BEET, WHEAT, POTATO, 0.1D);
        assertEquals(List.of(WHEAT + ">" + CARROT + ">" + POTATO),
                JournalMutationRules.revealedTriples(
                        Set.of(WHEAT, CARROT, POTATO), List.of(secret, known)));
        assertEquals(List.of(), JournalMutationRules.revealedTriples(
                Set.of(WHEAT, POTATO), List.of(known, secret)));
    }

    @Test
    void mutationEntriesStaySortedAndDeduplicated() {
        MutationDefinition first = new MutationDefinition(POTATO, WHEAT, CARROT, 0.1D);
        MutationDefinition second = new MutationDefinition(BEET, CARROT, POTATO, 0.2D);
        List<String> results = JournalMutationRules.revealedTriples(
                Set.of(WHEAT, CARROT, POTATO, BEET), List.of(first, second, first));
        assertEquals(2, results.size());
        assertEquals(results.stream().sorted().toList(), results);
    }
    @Test
    void datapackReloadRebuildsHintsWithoutErasingDiscoveries() {
        Set<String> discovered = Set.of(WHEAT, CARROT, POTATO);
        MutationDefinition active = new MutationDefinition(POTATO, WHEAT, CARROT, 0.1D);
        List<String> before = JournalMutationRules.revealedTriples(
                discovered, List.of(active));
        assertEquals(List.of(WHEAT + ">" + CARROT + ">" + POTATO), before);

        // The recipe was removed by /reload, not the research progress.
        List<String> after = JournalMutationRules.revealedTriples(
                discovered, List.of());
        assertTrue(after.isEmpty());
        assertEquals(3, discovered.size());
        assertEquals(before, JournalMutationRules.revealedTriples(
                discovered, List.of(active)));
    }

}

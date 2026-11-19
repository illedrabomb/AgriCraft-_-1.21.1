package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrossParentPairRulesTest {
    private static Genome of(String plant) {
        return Genome.basic("cropgenetics:" + plant + "_plant");
    }

    @Test
    void twoParentsStillMatchTheirPair() {
        assertTrue(CrossParentPairRules.matchesAnyPair(
                List.of(of("a"), of("b")),
                (first, second) -> first.endsWith("a_plant")
                        && second.endsWith("b_plant")));
    }

    @Test
    void mutationCanUseThirdParentEvenWhenFirstTwoDoNotMatch() {
        assertTrue(CrossParentPairRules.matchesAnyPair(
                List.of(of("a"), of("b"), of("c")),
                (first, second) -> first.endsWith("b_plant")
                        && second.endsWith("c_plant")));
    }

    @Test
    void mutationCanUseFourthParentWithEarlierParent() {
        assertTrue(CrossParentPairRules.matchesAnyPair(
                List.of(of("a"), of("b"), of("c"), of("d")),
                (first, second) -> first.endsWith("a_plant")
                        && second.endsWith("d_plant")));
    }

    @Test
    void unrelatedRecipeDoesNotMatchAnyCombination() {
        assertFalse(CrossParentPairRules.matchesAnyPair(
                List.of(of("a"), of("b"), of("c"), of("d")),
                (first, second) -> first.endsWith("x_plant")
                        && second.endsWith("y_plant")));
    }

    @Test
    void oneParentCannotPairWithItself() {
        assertFalse(CrossParentPairRules.matchesAnyPair(
                List.of(of("a")), (first, second) -> true));
    }
}

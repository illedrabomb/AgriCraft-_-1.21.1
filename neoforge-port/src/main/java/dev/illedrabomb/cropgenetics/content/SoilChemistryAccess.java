package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Optional;

/**
 * Optional server-side soil probe boundary. No vanilla farmland chemistry is
 * invented, and no world state is stored in the core until a provider exists.
 */
public final class SoilChemistryAccess {
    private SoilChemistryAccess() {}

    public record Sample(SoilChemistryRule.Humidity humidity,
                         SoilChemistryRule.Acidity acidity,
                         SoilChemistryRule.Nutrients nutrients) {
        public Sample {
            Objects.requireNonNull(humidity);
            Objects.requireNonNull(acidity);
            Objects.requireNonNull(nutrients);
        }
    }

    @FunctionalInterface
    public interface Provider {
        /** The supplied position is the supporting soil block, not the crop. */
        Optional<Sample> sample(ServerLevel level, BlockPos soilPos);
    }

    private static volatile Provider provider;

    public static void install(Provider implementation) {
        provider = Objects.requireNonNull(implementation);
    }

    public static void uninstall() {
        provider = null;
    }

    public static boolean matches(ServerLevel level, BlockPos soilPos,
                                  SoilChemistryRule rule, int strength) {
        if (!rule.configured()) return true;
        Provider active = provider;
        if (active == null) return false; // Unknown chemistry is NOT success.
        Optional<Sample> result = active.sample(level, soilPos);
        if (result == null || result.isEmpty()) return false;
        Sample sample = result.get();
        return rule.humidity().map(c -> SoilChemistryRule.matches(
                    SoilChemistryRule.Humidity.class, sample.humidity(), c, strength)).orElse(true)
                && rule.acidity().map(c -> SoilChemistryRule.matches(
                    SoilChemistryRule.Acidity.class, sample.acidity(), c, strength)).orElse(true)
                && rule.nutrients().map(c -> SoilChemistryRule.matches(
                    SoilChemistryRule.Nutrients.class, sample.nutrients(), c, strength)).orElse(true);
    }
}

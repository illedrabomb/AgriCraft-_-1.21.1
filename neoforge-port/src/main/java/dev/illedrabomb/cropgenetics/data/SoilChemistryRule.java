package dev.illedrabomb.cropgenetics.data;

import java.util.Locale;
import java.util.Optional;

/**
 * Explicit qualitative crop soil chemistry. This is a NeoForge port model,
 * NOT a reconstruction of AgriCraft 1.18.2 tolerance_factor formulas.
 */
public record SoilChemistryRule(Optional<Condition> humidity,
                                Optional<Condition> acidity,
                                Optional<Condition> nutrients) {
    public static final SoilChemistryRule UNRESTRICTED =
            new SoilChemistryRule(Optional.empty(), Optional.empty(), Optional.empty());

    public enum Humidity { ARID, DRY, NORMAL, WET, SATURATED }
    public enum Acidity { ACIDIC, SLIGHTLY_ACIDIC, NEUTRAL, SLIGHTLY_ALKALINE, ALKALINE }
    public enum Nutrients { LOW, MEDIUM, HIGH }

    public enum Comparison { EQUAL, EQUAL_OR_HIGHER, EQUAL_OR_LOWER }

    /** A comparison against an explicit category; ranking is category-specific. */
    public record Condition(String category, Comparison comparison) {
        public Condition {
            if (category == null || category.isBlank() || category.length() > 32)
                throw new IllegalArgumentException("Invalid chemistry category");
            if (comparison == null)
                throw new IllegalArgumentException("Missing chemistry comparison");
        }
    }

    public SoilChemistryRule {
        if (humidity == null || acidity == null || nutrients == null)
            throw new IllegalArgumentException("Chemistry optionals must be non-null");
        humidity.ifPresent(c -> parse(Humidity.class, c.category()));
        acidity.ifPresent(c -> parse(Acidity.class, c.category()));
        nutrients.ifPresent(c -> parse(Nutrients.class, c.category()));
    }

    public boolean configured() {
        return humidity.isPresent() || acidity.isPresent() || nutrients.isPresent();
    }

    public static <E extends Enum<E>> E parse(Class<E> type, String name) {
        try {
            return Enum.valueOf(type, name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported " + type.getSimpleName()
                    + " chemistry category: " + name, exception);
        }
    }

    public static <E extends Enum<E>> boolean matches(Class<E> type, E actual, Condition requirement) {
        E expected = parse(type, requirement.category());
        return switch (requirement.comparison()) {
            case EQUAL -> actual == expected;
            case EQUAL_OR_HIGHER -> actual.ordinal() >= expected.ordinal();
            case EQUAL_OR_LOWER -> actual.ordinal() <= expected.ordinal();
        };
    }
}

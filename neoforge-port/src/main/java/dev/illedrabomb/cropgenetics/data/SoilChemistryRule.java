package dev.illedrabomb.cropgenetics.data;

import java.util.Locale;
import java.util.Optional;

/**
 * Soil categories follow the documented AgriCraft 1.18 ordinal scales.
 * Strength tolerance widens comparisons by toleranceFactor * expressed Strength.
 * This does not itself simulate a changing soil environment.
 */
public record SoilChemistryRule(Optional<Condition> humidity,
                                Optional<Condition> acidity,
                                Optional<Condition> nutrients) {
    public static final SoilChemistryRule UNRESTRICTED =
            new SoilChemistryRule(Optional.empty(), Optional.empty(), Optional.empty());

    public enum Humidity { ARID, DRY, DAMP, WET, WATERY, FLOODED }
    public enum Acidity {
        HIGHLY_ACIDIC, ACIDIC, SLIGHTLY_ACIDIC, NEUTRAL,
        SLIGHTLY_ALKALINE, ALKALINE, HIGHLY_ALKALINE
    }
    public enum Nutrients { NONE, VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH }
    public enum Comparison { EQUAL, EQUAL_OR_HIGHER, EQUAL_OR_LOWER }

    /** Factor in [0,1]; zero reproduces the pre-tolerance exact comparison. */
    public record Condition(String category, Comparison comparison, double toleranceFactor) {
        public Condition(String category, Comparison comparison) {
            this(category, comparison, 0.0D);
        }

        public Condition {
            if (category == null || category.isBlank() || category.length() > 32)
                throw new IllegalArgumentException("Invalid chemistry category");
            if (comparison == null)
                throw new IllegalArgumentException("Missing chemistry comparison");
            if (!Double.isFinite(toleranceFactor) || toleranceFactor < 0 || toleranceFactor > 1)
                throw new IllegalArgumentException("Chemistry tolerance factor must be within 0..1");
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
        if (name == null) throw new IllegalArgumentException("Missing soil category");
        String value = name.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        // Backward-compatible aliases for profiles/overrides created by the
        // initial NeoForge prototype, as well as documented AgriCraft aliases.
        if (type == Humidity.class) {
            if (value.equals("NORMAL") || value.equals("MOIST")) value = "DAMP";
            if (value.equals("SATURATED")) value = "FLOODED";
            if (value.equals("STANDARD") || value.equals("DEFAULT")) value = "WET";
        } else if (type == Nutrients.class) {
            if (value.equals("NORMAL") || value.equals("AVERAGE")) value = "MEDIUM";
            if (value.equals("RICH")) value = "VERY_HIGH";
            if (value.equals("EMPTY") || value.equals("ZERO")) value = "NONE";
            if (value.equals("POOR") || value.equals("SCARCE")) value = "VERY_LOW";
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported " + type.getSimpleName()
                    + " chemistry category: " + name, exception);
        }
    }

    public static <E extends Enum<E>> boolean matches(Class<E> type, E actual,
                                                        Condition requirement) {
        return matches(type, actual, requirement, 1);
    }

    /** The original documented interval: target +/- toleranceFactor * Strength. */
    public static <E extends Enum<E>> boolean matches(Class<E> type, E actual,
                                                        Condition requirement, int strength) {
        if (strength < 1 || strength > 10)
            throw new IllegalArgumentException("Expressed Strength must be within 1..10");
        E expected = parse(type, requirement.category());
        double tolerance = requirement.toleranceFactor() * strength;
        return switch (requirement.comparison()) {
            case EQUAL -> actual.ordinal() >= expected.ordinal() - tolerance
                    && actual.ordinal() <= expected.ordinal() + tolerance;
            case EQUAL_OR_HIGHER -> actual.ordinal() >= expected.ordinal() - tolerance;
            case EQUAL_OR_LOWER -> actual.ordinal() <= expected.ordinal() + tolerance;
        };
    }
}

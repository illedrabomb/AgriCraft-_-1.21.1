package dev.illedrabomb.cropgenetics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server/world configuration. Historical config key names remain stable
 * when the breeding rule evolves from two-parent rows to 2..4 parents.
 */
public final class CropBalanceConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ROW_CROSS_EVOLUTION;
    public static final ModConfigSpec.DoubleValue ROW_EVOLUTION_MUTATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue CROSS_FERTILITY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue GOURD_FRUIT_CHANCE_MULTIPLIER;

    private CropBalanceConfig() {}

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("breeding");
        ROW_CROSS_EVOLUTION = b.comment(
                "Allow visible-stat evolution when 2, 3 or 4 mature parents",
                "produce offspring on double sticks. Key name kept for old configs.")
                .define("rowCrossEvolution", true);
        ROW_EVOLUTION_MUTATION_MULTIPLIER = b.comment(
                "Multiplier for the shared 0-4 genetic-point pool and negative",
                "stat losses (not the old independent per-stat mutation odds).",
                "The 2/3/4-parent and straight-row point tables already contain",
                "their normal bonuses. 0 disables numeric mutations, not",
                "parent-template inheritance. Higher values may boost gains",
                "and losses; no stat can gain more than 3 points per child.")
                .defineInRange("rowEvolutionMutationMultiplier", 1.0D, 0.0D, 5.0D);
        CROSS_FERTILITY_MULTIPLIER = b.comment(
                "Multiplier for crossing success. Three/four parents get an extra",
                "25%/50% bonus compared with the two-parent baseline.")
                .defineInRange("crossFertilityMultiplier", 1.0D, 0.1D, 5.0D);
        b.pop();

        b.push("gourds");
        GOURD_FRUIT_CHANCE_MULTIPLIER = b.comment(
                "Multiplier for eligible mature melon/pumpkin fruit rolls.")
                .defineInRange("fruitChanceMultiplier", 1.0D, 0.1D, 5.0D);
        b.pop();
        SPEC = b.build();
    }
}

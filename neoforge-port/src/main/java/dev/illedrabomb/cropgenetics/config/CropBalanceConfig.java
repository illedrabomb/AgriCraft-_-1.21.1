package dev.illedrabomb.cropgenetics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server/world configuration. Defaults preserve the existing breeding and
 * gourd balance; row-cross evolution can be independently disabled.
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
                "Allow mutation of offspring genes when two same-species parents",
                "crossbreed through a double-stick target in a straight row.")
                .define("rowCrossEvolution", true);
        ROW_EVOLUTION_MUTATION_MULTIPLIER = b.comment(
                "Mutation probability multiplier for same-species straight-row crosses.",
                "0 disables mutations; 1 uses the normal gene-based probability.")
                .defineInRange("rowEvolutionMutationMultiplier", 1.0D, 0.0D, 5.0D);
        CROSS_FERTILITY_MULTIPLIER = b.comment(
                "Multiplier for successful two-parent breeding attempts.")
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

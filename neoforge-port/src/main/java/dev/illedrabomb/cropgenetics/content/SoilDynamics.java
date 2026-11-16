package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
import dev.illedrabomb.cropgenetics.data.SoilDynamicsSettings;
import dev.illedrabomb.cropgenetics.data.SoilOverrideSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * First bounded, opt-in soil evolution rules. Applies ONLY to explicitly
 * profiled soil beneath active crop sticks; never infers vanilla chemistry.
 * These probabilities are new game design defaults, NOT AgriCore formulas.
 */
public final class SoilDynamics {
    private SoilDynamics() {}

    private static volatile SoilDynamicsSettings settings = SoilDynamicsSettings.DEFAULT;

    /** Atomically replace the server settings after a successful datapack reload. */
    public static void configure(SoilDynamicsSettings updated) {
        settings = java.util.Objects.requireNonNull(updated);
    }

    /** Rain may raise humidity according to the loaded server datapack rate. */
    public static void onRandomTick(ServerLevel level, BlockPos cropPos, RandomSource random) {
        int chance = settings.rainTickChance();
        if (chance == 0 || !level.isRainingAt(cropPos.above())
                || random.nextInt(chance) != 0) return;
        change(level, cropPos.below(), true);
    }

    /** Successful growth may reduce nutrients one level, never below NONE. */
    public static void onGrowth(ServerLevel level, BlockPos cropPos, RandomSource random) {
        int chance = settings.growthNutrientChance();
        if (chance > 0 && random.nextInt(chance) == 0)
            change(level, cropPos.below(), false);
    }

    private static boolean change(ServerLevel level, BlockPos soilPos, boolean wet) {
        if (!level.isInWorldBounds(soilPos) || !level.hasChunkAt(soilPos)) return false;
        BlockState soil = level.getBlockState(soilPos);
        SoilOverrideSavedData saved = SoilOverrideSavedData.get(level);
        Optional<SoilChemistryAccess.Sample> current = saved.sample(soilPos, soil)
                .or(() -> CropGeneticsMod.soilProfiles().sample(soil));
        if (current.isEmpty()) return false;
        SoilChemistryAccess.Sample sample = current.get();
        SoilChemistryAccess.Sample next;
        if (wet) {
            SoilChemistryRule.Humidity[] values = SoilChemistryRule.Humidity.values();
            int index = Math.min(values.length - 1, sample.humidity().ordinal() + 1);
            if (index == sample.humidity().ordinal()) return false;
            next = new SoilChemistryAccess.Sample(values[index], sample.acidity(), sample.nutrients());
        } else {
            SoilChemistryRule.Nutrients[] values = SoilChemistryRule.Nutrients.values();
            int index = Math.max(0, sample.nutrients().ordinal() - 1);
            if (index == sample.nutrients().ordinal()) return false;
            next = new SoilChemistryAccess.Sample(sample.humidity(), sample.acidity(), values[index]);
        }
        return saved.put(soilPos, soil, next);
    }
}

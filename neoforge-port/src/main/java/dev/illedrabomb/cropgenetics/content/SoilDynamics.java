package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
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

    private static final int RAIN_TICK_CHANCE = 16;
    private static final int GROWTH_DEPLETION_CHANCE = 8;

    /** Rain raises humidity one category on average every 16 crop random ticks. */
    public static void onRandomTick(ServerLevel level, BlockPos cropPos, RandomSource random) {
        if (!level.isRainingAt(cropPos.above()) || random.nextInt(RAIN_TICK_CHANCE) != 0)
            return;
        change(level, cropPos.below(), true);
    }

    /** Successful growth may reduce nutrients one level, never below NONE. */
    public static void onGrowth(ServerLevel level, BlockPos cropPos, RandomSource random) {
        if (random.nextInt(GROWTH_DEPLETION_CHANCE) == 0)
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

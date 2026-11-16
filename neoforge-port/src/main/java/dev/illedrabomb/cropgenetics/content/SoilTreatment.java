package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
import dev.illedrabomb.cropgenetics.data.SoilOverrideSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;

/**
 * Deliberately simple first playable treatment:
 * - water bucket: humidity +1 qualitative level, capped at FLOODED;
 * - bone meal: nutrients +1 qualitative level, capped at VERY_HIGH.
 * No implicit base chemistry and no claim of AgriCore parity.
 */
public final class SoilTreatment {
    public enum Treatment { WATER, FERTILIZER }
    private SoilTreatment() {}

    /**
     * Returns true only when an actual change was saved. No samples are
     * created for blocks without explicit profiles or stored overrides.
     * The caller is responsible for consuming the used item after success.
     */
    public static boolean apply(ServerLevel level, BlockPos soilPos, Treatment treatment) {
        if (!level.isInWorldBounds(soilPos) || !level.hasChunkAt(soilPos))
            return false;
        BlockState soil = level.getBlockState(soilPos);
        SoilOverrideSavedData saved = SoilOverrideSavedData.get(level);
        Optional<SoilChemistryAccess.Sample> effective = saved.sample(soilPos, soil)
                .or(() -> CropGeneticsMod.soilProfiles().sample(soil));
        if (effective.isEmpty()) return false;
        SoilChemistryAccess.Sample current = effective.get();
        SoilChemistryAccess.Sample updated;
        if (treatment == Treatment.WATER) {
            SoilChemistryRule.Humidity[] levels = SoilChemistryRule.Humidity.values();
            int next = Math.min(current.humidity().ordinal() + 1, levels.length - 1);
            if (next == current.humidity().ordinal()) return false;
            updated = new SoilChemistryAccess.Sample(levels[next], current.acidity(), current.nutrients());
        } else {
            SoilChemistryRule.Nutrients[] levels = SoilChemistryRule.Nutrients.values();
            int next = Math.min(current.nutrients().ordinal() + 1, levels.length - 1);
            if (next == current.nutrients().ordinal()) return false;
            updated = new SoilChemistryAccess.Sample(current.humidity(), current.acidity(), levels[next]);
        }
        return saved.put(soilPos, soil, updated);
    }
}

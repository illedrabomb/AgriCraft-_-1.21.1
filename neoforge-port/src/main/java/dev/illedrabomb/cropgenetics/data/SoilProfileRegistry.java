package dev.illedrabomb.cropgenetics.data;

import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable, server-defined soil profiles. No profiles are inferred from
 * vanilla hydration, and all unloaded/unspecified blocks have no sample.
 */
public final class SoilProfileRegistry {
    private volatile Map<String, SoilChemistryAccess.Sample> profiles = Map.of();

    public void replaceAll(Map<String, SoilChemistryAccess.Sample> incoming) {
        profiles = Map.copyOf(incoming);
    }

    public int size() {
        return profiles.size();
    }

    public Optional<SoilChemistryAccess.Sample> sample(BlockState soil) {
        String blockId = BuiltInRegistries.BLOCK.getKey(soil.getBlock()).toString();
        return Optional.ofNullable(profiles.get(blockId));
    }
}

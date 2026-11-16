package dev.illedrabomb.cropgenetics.data;

import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;

/**
 * One immutable snapshot holds both soil chemistry and growth modifiers.
 * No profile is inferred unless a server datapack explicitly declares it.
 */
public final class SoilProfileRegistry {
    public record Profile(SoilChemistryAccess.Sample chemistry, double growthModifier) {
        public Profile {
            if (chemistry == null || !Double.isFinite(growthModifier)
                    || growthModifier < 0.0D || growthModifier > 4.0D)
                throw new IllegalArgumentException("Invalid soil profile/growth_modifier");
        }
    }

    private volatile Map<String, Profile> profiles = Map.of();

    public void replaceProfiles(Map<String, Profile> incoming) {
        profiles = Map.copyOf(incoming);
    }

    /** Existing API: replace chemistry alone, without a growth penalty/bonus. */
    public void replaceAll(Map<String, SoilChemistryAccess.Sample> incoming) {
        java.util.Map<String, Profile> converted = new java.util.HashMap<>();
        incoming.forEach((id, chemistry) -> converted.put(id, new Profile(chemistry, 1.0D)));
        replaceProfiles(converted);
    }

    public int size() {
        return profiles.size();
    }

    public Optional<Profile> profile(BlockState soil) {
        String blockId = BuiltInRegistries.BLOCK.getKey(soil.getBlock()).toString();
        return Optional.ofNullable(profiles.get(blockId));
    }

    public Optional<SoilChemistryAccess.Sample> sample(BlockState soil) {
        return profile(soil).map(Profile::chemistry);
    }

    /**
     * The modifier is tied to the current block type, not stored chemistry
     * overrides; no profile means ordinary vanilla-independent growth factor 1.
     */
    public double growthModifier(BlockState soil) {
        return profile(soil).map(Profile::growthModifier).orElse(1.0D);
    }
}

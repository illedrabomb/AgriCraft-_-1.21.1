package dev.illedrabomb.cropgenetics.data;

import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
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

    /** Both maps publish together to avoid partial reload states. */
    private record Snapshot(Map<String, Profile> blocks, Map<String, Profile> tags) {
        private Snapshot {
            blocks = Map.copyOf(blocks);
            tags = Map.copyOf(tags);
        }
    }
    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of());

    public void replaceProfiles(Map<String, Profile> incoming) {
        replaceProfiles(incoming, Map.of());
    }

    public void replaceProfiles(Map<String, Profile> blocks, Map<String, Profile> tags) {
        snapshot = new Snapshot(blocks, tags);
    }

    /** Existing API: replace chemistry alone, without a growth penalty/bonus. */
    public void replaceAll(Map<String, SoilChemistryAccess.Sample> incoming) {
        java.util.Map<String, Profile> converted = new java.util.HashMap<>();
        incoming.forEach((id, chemistry) -> converted.put(id, new Profile(chemistry, 1.0D)));
        replaceProfiles(converted);
    }

    public int size() {
        Snapshot active = snapshot;
        return active.blocks().size() + active.tags().size();
    }

    public Optional<Profile> profile(BlockState soil) {
        Snapshot active = snapshot;
        String blockId = BuiltInRegistries.BLOCK.getKey(soil.getBlock()).toString();
        Profile exact = active.blocks().get(blockId);
        if (exact != null) return Optional.of(exact);

        // A block can belong to several common tags. Never choose an
        // arbitrary profile when more than one configured tag matches.
        Profile result = null;
        for (Map.Entry<String, Profile> entry : active.tags().entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id == null || !soil.is(TagKey.create(Registries.BLOCK, id))) continue;
            if (result != null) return Optional.empty();
            result = entry.getValue();
        }
        return Optional.ofNullable(result);
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

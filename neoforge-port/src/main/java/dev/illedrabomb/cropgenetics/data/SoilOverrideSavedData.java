package dev.illedrabomb.cropgenetics.data;

import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Per-dimension, per-position soil overrides. Explicitly edited entries take
 * precedence over static datapack soil profiles, and survive server restarts.
 * Stores only qualitative values, not invented vanilla hydration chemistry.
 */
public final class SoilOverrideSavedData extends SavedData {
    private static final String FILE_NAME = "cropgenetics_soil_overrides";
    private static final int MAX_ENTRIES = 65536;
    private record Entry(String blockId, SoilChemistryAccess.Sample sample) {}
    private final Map<Long, Entry> values = new HashMap<>();

    private SoilOverrideSavedData() {}

    public static SoilOverrideSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SoilOverrideSavedData::new, SoilOverrideSavedData::load),
                FILE_NAME);
    }

    public Optional<SoilChemistryAccess.Sample> sample(BlockPos soilPos, BlockState currentSoil) {
        Entry entry = values.get(soilPos.asLong());
        if (entry == null) return Optional.empty();
        String currentBlockId = BuiltInRegistries.BLOCK.getKey(currentSoil.getBlock()).toString();
        if (!entry.blockId().equals(currentBlockId)) {
            // Do not apply old chemistry to a different block later placed at
            // the same coordinates; discard stale overrides permanently.
            remove(soilPos);
            return Optional.empty();
        }
        return Optional.of(entry.sample());
    }

    /** Only explicit server-side calls can mutate soil chemistry. */
    public boolean put(BlockPos soilPos, BlockState currentSoil,
                       SoilChemistryAccess.Sample sample) {
        if (!values.containsKey(soilPos.asLong()) && values.size() >= MAX_ENTRIES)
            return false;
        Entry replacement = new Entry(
                BuiltInRegistries.BLOCK.getKey(currentSoil.getBlock()).toString(), sample);
        Entry old = values.put(soilPos.asLong(), replacement);
        if (!replacement.equals(old)) setDirty();
        return true;
    }

    public boolean remove(BlockPos soilPos) {
        if (values.remove(soilPos.asLong()) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public int size() { return values.size(); }

    private static SoilOverrideSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        SoilOverrideSavedData data = new SoilOverrideSavedData();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && data.values.size() < MAX_ENTRIES; i++) {
            CompoundTag row = list.getCompound(i);
            try {
                long position = row.getLong("position");
                SoilChemistryAccess.Sample sample = new SoilChemistryAccess.Sample(
                        SoilChemistryRule.parse(SoilChemistryRule.Humidity.class,
                                row.getString("humidity")),
                        SoilChemistryRule.parse(SoilChemistryRule.Acidity.class,
                                row.getString("acidity")),
                        SoilChemistryRule.parse(SoilChemistryRule.Nutrients.class,
                                row.getString("nutrients")));
                String blockId = row.getString("block");
                if (blockId.isBlank() || net.minecraft.resources.ResourceLocation.tryParse(blockId) == null)
                    continue;
                // Avoid corrupt rows and duplicate positions from overwriting a
                // previously valid entry during deserialization.
                data.values.putIfAbsent(position, new Entry(blockId, sample));
            } catch (IllegalArgumentException ignored) {
                // Invalid saved rows do not prevent loading the remaining entries.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        values.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag row = new CompoundTag();
            row.putLong("position", entry.getKey());
            row.putString("block", entry.getValue().blockId());
            row.putString("humidity", entry.getValue().sample().humidity().name().toLowerCase(java.util.Locale.ROOT));
            row.putString("acidity", entry.getValue().sample().acidity().name().toLowerCase(java.util.Locale.ROOT));
            row.putString("nutrients", entry.getValue().sample().nutrients().name().toLowerCase(java.util.Locale.ROOT));
            entries.add(row);
        });
        tag.put("entries", entries);
        return tag;
    }
}

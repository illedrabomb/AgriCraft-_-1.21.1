package dev.illedrabomb.cropgenetics.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Bounded, versioned public rules format. No item components or player data. */
public final class PlantConditionsWire {
    public static final int VERSION = 1;
    public static final int MAX_BYTES = 262_144;
    private static final int MAX_ID = 128;
    private PlantConditionsWire() {}

    public static byte[] encode(PlantConditionsView view) {
        try {
            var buffer = new ByteArrayOutputStream();
            var out = new DataOutputStream(buffer);
            out.writeInt(VERSION);
            out.writeInt(view.plants().size());
            for (var entry : view.plants()) {
                writeString(out, entry.plantId());
                writeStrings(out, entry.seedItems());
                out.writeByte(entry.siteRule().ordinal());
                var rules = entry.requirements();
                out.writeByte(rules.minLight());
                out.writeByte(rules.maxLight());
                out.writeByte(rules.lightTolerance());
                writeOptional(out, rules.soilBlock());
                writeOptional(out, rules.soilTag());
                writeStrings(out, rules.allowedBiomes());
                writeStrings(out, rules.blockedBiomes());
                writeStrings(out, rules.allowedDimensions());
                writeStrings(out, rules.blockedDimensions());
                var env = rules.environment();
                writeInteger(out, env.minY());
                writeInteger(out, env.maxY());
                writeOptional(out, env.fluidId());
                writeStrings(out, rules.seasonRule().allowedSeasons());
                writeCondition(out, rules.soilChemistry().humidity());
                writeCondition(out, rules.soilChemistry().acidity());
                writeCondition(out, rules.soilChemistry().nutrients());
                if (buffer.size() > MAX_BYTES)
                    throw new IllegalArgumentException("Plant conditions exceed wire size limit");
            }
            out.flush();
            // Check the complete frame as well, including its version/count
            // header. This also protects future changes to the wire trailer.
            if (buffer.size() > MAX_BYTES)
                throw new IllegalArgumentException("Plant conditions exceed wire size limit");
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static PlantConditionsView decode(byte[] data) {
        if (data.length > MAX_BYTES) throw new IllegalArgumentException("Oversized plant rules");
        try {
            var in = new DataInputStream(new ByteArrayInputStream(data));
            if (in.readInt() != VERSION) throw new IllegalArgumentException("Unknown plant rules version");
            int count = in.readInt();
            if (count < 0 || count > PlantConditionsView.MAX_PLANTS)
                throw new IllegalArgumentException("Invalid plant count");
            var entries = new ArrayList<PlantConditionsView.Entry>(count);
            for (int i = 0; i < count; i++) {
                String id = readString(in);
                List<String> seeds = readStrings(in, 64);
                int siteRule = in.readUnsignedByte();
                if (siteRule >= PlantConditionsView.SiteRule.values().length)
                    throw new IllegalArgumentException("Invalid site rule");
                int minLight = in.readUnsignedByte();
                int maxLight = in.readUnsignedByte();
                int tolerance = in.readUnsignedByte();
                var soilBlock = readOptional(in);
                var soilTag = readOptional(in);
                var biomesAllowed = readStrings(in, 64);
                var biomesBlocked = readStrings(in, 64);
                var dimensionsAllowed = readStrings(in, 64);
                var dimensionsBlocked = readStrings(in, 64);
                var env = new EnvironmentFilter(readInteger(in), readInteger(in), readOptional(in));
                var seasons = new SeasonRule(readStrings(in, 4));
                var chemistry = new SoilChemistryRule(readCondition(in), readCondition(in), readCondition(in));
                var rules = new PlantRequirements(minLight, maxLight, soilBlock, soilTag, tolerance,
                        biomesAllowed, biomesBlocked, dimensionsAllowed, dimensionsBlocked,
                        env, seasons, chemistry);
                entries.add(new PlantConditionsView.Entry(id, seeds, rules,
                        PlantConditionsView.SiteRule.values()[siteRule]));
            }
            if (in.available() != 0) throw new IllegalArgumentException("Trailing plant rule bytes");
            return new PlantConditionsView(entries);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid plant rules data", ex);
        }
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        if (value == null || value.length() > MAX_ID)
            throw new IllegalArgumentException("Invalid plant rule string");
        out.writeUTF(value);
    }

    private static String readString(DataInputStream in) throws IOException {
        String value = in.readUTF();
        if (value.length() > MAX_ID) throw new IllegalArgumentException("Oversized plant rule string");
        return value;
    }

    private static void writeStrings(DataOutputStream out, List<String> values) throws IOException {
        if (values.size() > 64) throw new IllegalArgumentException("Oversized plant rule list");
        out.writeByte(values.size());
        for (String value : values) writeString(out, value);
    }

    private static List<String> readStrings(DataInputStream in, int max) throws IOException {
        int size = in.readUnsignedByte();
        if (size > max) throw new IllegalArgumentException("Oversized plant rule list");
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) values.add(readString(in));
        return List.copyOf(values);
    }

    private static void writeOptional(DataOutputStream out, Optional<String> value) throws IOException {
        out.writeBoolean(value.isPresent());
        if (value.isPresent()) writeString(out, value.get());
    }

    private static Optional<String> readOptional(DataInputStream in) throws IOException {
        return in.readBoolean() ? Optional.of(readString(in)) : Optional.empty();
    }

    private static void writeInteger(DataOutputStream out, Optional<Integer> value) throws IOException {
        out.writeBoolean(value.isPresent());
        if (value.isPresent()) out.writeInt(value.get());
    }

    private static Optional<Integer> readInteger(DataInputStream in) throws IOException {
        return in.readBoolean() ? Optional.of(in.readInt()) : Optional.empty();
    }

    private static void writeCondition(DataOutputStream out,
                                       Optional<SoilChemistryRule.Condition> value) throws IOException {
        out.writeBoolean(value.isPresent());
        if (value.isPresent()) {
            var condition = value.get();
            writeString(out, condition.category());
            out.writeByte(condition.comparison().ordinal());
            out.writeDouble(condition.toleranceFactor());
        }
    }

    private static Optional<SoilChemistryRule.Condition> readCondition(DataInputStream in) throws IOException {
        if (!in.readBoolean()) return Optional.empty();
        String category = readString(in);
        int comparison = in.readUnsignedByte();
        if (comparison >= SoilChemistryRule.Comparison.values().length)
            throw new IllegalArgumentException("Invalid chemistry comparison");
        return Optional.of(new SoilChemistryRule.Condition(category,
                SoilChemistryRule.Comparison.values()[comparison], in.readDouble()));
    }
}

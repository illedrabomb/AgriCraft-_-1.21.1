package dev.illedrabomb.cropgenetics.genetics;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Three-stat persistence, with a read-only migration path for legacy five-pair genomes. */
public final class GenomeCodecs {
    private GenomeCodecs() {}

    private static final Codec<GenePair> OLD_GENE_PAIR = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.intRange(1, 10).fieldOf("dominant").forGetter(GenePair::dominant),
                    Codec.intRange(1, 10).fieldOf("recessive").forGetter(GenePair::recessive)
            ).apply(instance, GenePair::new));

    /** Only used when reading older world saves or item components. */
    private record LegacyGenome(String plant, GenePair growth, GenePair gain,
                                GenePair strength, GenePair fertility, GenePair mutativity) {
        Genome migrate() {
            // The displayed allele in the old UI was the numerically higher one.
            return new Genome(plant, growth.dominant(), gain.dominant(), strength.dominant());
        }
    }

    private static final Codec<LegacyGenome> LEGACY = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("plant").forGetter(LegacyGenome::plant),
                    OLD_GENE_PAIR.fieldOf("growth").forGetter(LegacyGenome::growth),
                    OLD_GENE_PAIR.fieldOf("gain").forGetter(LegacyGenome::gain),
                    OLD_GENE_PAIR.fieldOf("strength").forGetter(LegacyGenome::strength),
                    OLD_GENE_PAIR.fieldOf("fertility").forGetter(LegacyGenome::fertility),
                    OLD_GENE_PAIR.fieldOf("mutativity").forGetter(LegacyGenome::mutativity)
            ).apply(instance, LegacyGenome::new));

    private static final Codec<Genome> THREE_STATS = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("plant").forGetter(Genome::plantId),
                    Codec.intRange(1, 10).fieldOf("growth").forGetter(Genome::growth),
                    Codec.intRange(1, 10).fieldOf("gain").forGetter(Genome::gain),
                    Codec.intRange(1, 10).fieldOf("strength").forGetter(Genome::strength)
            ).apply(instance, Genome::new));

    // Encode the simple format. Decode either the new format or an older
    // five-pair compound, including the old custom item data component format.
    public static final Codec<Genome> GENOME = Codec.either(THREE_STATS, LEGACY)
            .xmap(e -> e.map(g -> g, LegacyGenome::migrate), Either::left);

    public static final StreamCodec<RegistryFriendlyByteBuf, Genome> STREAM_CODEC = StreamCodec.of(
            GenomeCodecs::writeGenome, GenomeCodecs::readGenome);

    private static void writeGenome(RegistryFriendlyByteBuf buf, Genome genome) {
        buf.writeUtf(genome.plantId(), 128);
        buf.writeVarInt(genome.growth());
        buf.writeVarInt(genome.gain());
        buf.writeVarInt(genome.strength());
    }

    private static Genome readGenome(RegistryFriendlyByteBuf buf) {
        return new Genome(buf.readUtf(128), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
}

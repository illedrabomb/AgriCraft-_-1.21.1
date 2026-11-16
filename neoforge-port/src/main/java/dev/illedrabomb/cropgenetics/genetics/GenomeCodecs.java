package dev.illedrabomb.cropgenetics.genetics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Persistent and network-compatible representation of a genetic seed.
 * The immutable Genome/GenePair domain records remain Minecraft-independent.
 */
public final class GenomeCodecs {
    private GenomeCodecs() {}

    public static final Codec<GenePair> GENE_PAIR = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.intRange(1, 10).fieldOf("dominant").forGetter(GenePair::dominant),
                    Codec.intRange(1, 10).fieldOf("recessive").forGetter(GenePair::recessive)
            ).apply(instance, GenePair::new));

    public static final Codec<Genome> GENOME = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("plant").forGetter(Genome::plantId),
                    GENE_PAIR.fieldOf("growth").forGetter(Genome::growth),
                    GENE_PAIR.fieldOf("gain").forGetter(Genome::gain),
                    GENE_PAIR.fieldOf("strength").forGetter(Genome::strength),
                    GENE_PAIR.fieldOf("fertility").forGetter(Genome::fertility),
                    GENE_PAIR.fieldOf("mutativity").forGetter(Genome::mutativity)
            ).apply(instance, Genome::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Genome> STREAM_CODEC = StreamCodec.of(
            GenomeCodecs::writeGenome,
            GenomeCodecs::readGenome
    );

    private static void writeGenome(RegistryFriendlyByteBuf buf, Genome genome) {
        // Namespaced plant IDs are bounded on the wire to avoid unbounded strings.
        if (genome.plantId().length() > 128) {
            throw new IllegalArgumentException("Plant ID exceeds 128 characters");
        }
        buf.writeUtf(genome.plantId(), 128);
        writePair(buf, genome.growth());
        writePair(buf, genome.gain());
        writePair(buf, genome.strength());
        writePair(buf, genome.fertility());
        writePair(buf, genome.mutativity());
    }

    private static Genome readGenome(RegistryFriendlyByteBuf buf) {
        return new Genome(buf.readUtf(128), readPair(buf), readPair(buf), readPair(buf),
                readPair(buf), readPair(buf));
    }

    private static void writePair(RegistryFriendlyByteBuf buf, GenePair pair) {
        buf.writeVarInt(pair.dominant());
        buf.writeVarInt(pair.recessive());
    }

    private static GenePair readPair(RegistryFriendlyByteBuf buf) {
        return new GenePair(buf.readVarInt(), buf.readVarInt());
    }
}

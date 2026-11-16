package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Core items independent of the old InfinityLib item infrastructure. */
public final class CropItems {
    private CropItems() {}

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CropGeneticsMod.MOD_ID);

    public static final DeferredItem<GeneticSeedItem> GENETIC_SEED =
            ITEMS.registerItem("genetic_seed", GeneticSeedItem::new);

    public static final DeferredItem<SeedAnalyzerItem> SEED_ANALYZER =
            ITEMS.registerItem("seed_analyzer", SeedAnalyzerItem::new);

    public static final DeferredItem<BlockItem> CROP_STICKS =
            ITEMS.registerSimpleBlockItem("crop_sticks", CropBlocks.CROP_STICKS);

    /** Creates a genetic seed without losing the inherited genome. */
    public static ItemStack seedWithGenome(Genome genome) {
        ItemStack stack = new ItemStack(GENETIC_SEED.get());
        stack.set(CropComponents.GENOME.get(), genome);
        // Retain the source item while the active datapack still knows it.
        // Later recovery does not require the same crop adapter to exist.
        originalSeedId(genome).ifPresent(id -> stack.set(CropComponents.ORIGINAL_SEED.get(), id));
        return stack;
    }

    /**
     * Optional integration entry point: preserve the actual source item and
     * its analysis metadata during a lossless conversion to a genetic seed.
     * The source stack is never consumed or mutated.
     *
     * A pre-analyzed source is trusted only when the source genome exactly
     * matches the genome being written. Inheritance creates a NEW genome and
     * must never accidentally inherit a parent's analyzed flag.
     */
    public static ItemStack seedWithGenome(Genome genome, ItemStack sourceSeed) {
        ItemStack genetic = seedWithGenome(genome);
        if (sourceSeed == null || sourceSeed.isEmpty()) return genetic;

        Genome sourceGenome = sourceSeed.get(CropComponents.GENOME.get());
        // A source already carrying a DIFFERENT species or genotype cannot
        // lend its original item ID or analysis flag to this new seed.
        // Otherwise a mutation/cross could be wrongly recoverable as its
        // parent's seed after a datapack adapter is removed.
        if (sourceGenome != null && !genome.equals(sourceGenome)) return genetic;
        if (sourceGenome != null && isAnalyzed(sourceSeed)) {
            genetic.set(CropComponents.ANALYZED.get(), true);
        }

        // A genetic seed is not its own historical Vanilla/mod source. Keep
        // its explicitly recorded origin, never substitute cropgenetics:genetic_seed.
        if (sourceSeed.is(GENETIC_SEED.get())) {
            String recorded = sourceSeed.get(CropComponents.ORIGINAL_SEED.get());
            if (recorded != null) {
                genetic.set(CropComponents.ORIGINAL_SEED.get(), recorded);
            }
        } else if (sourceSeed.getItem() != Items.AIR) {
            String originalId = BuiltInRegistries.ITEM.getKey(sourceSeed.getItem()).toString();
            genetic.set(CropComponents.ORIGINAL_SEED.get(), originalId);
        }
        return genetic;
    }

    /** Choose a registered original seed from the current species definition. */
    private static Optional<String> originalSeedId(Genome genome) {
        PlantDefinition plant = CropGeneticsMod.plants().snapshot().plants().get(genome.plantId());
        if (plant == null) return Optional.empty();
        String unique = null;
        for (String candidate : plant.seedItems()) {
            ResourceLocation id = ResourceLocation.tryParse(candidate);
            if (id == null || BuiltInRegistries.ITEM.getOptional(id)
                    .filter(item -> item != Items.AIR).isEmpty()) continue;
            // Never guess a seed used by multiple species, even if that seed
            // appears as the only entry in this particular plant definition.
            if (!CropGeneticsMod.plants().snapshot().plantForSeed(candidate)
                    .map(found -> found.id().equals(genome.plantId())).orElse(false)) {
                continue;
            }
            // If a species has multiple possible seed item types, guessing
            // the first would risk turning one mod's item into another.
            if (unique != null && !unique.equals(candidate)) return Optional.empty();
            unique = candidate;
        }
        return Optional.ofNullable(unique);
    }

    /**
     * Explicit fallback conversion, never automatic: one genetic seed becomes
     * one plain Vanilla/mod seed. The resulting ItemStack deliberately carries
     * no genetics or analysis components, matching the original item behavior.
     *
     * For older stacks without original_seed, use the currently active plant
     * catalogue. If neither lookup works, do not consume the genetic seed.
     */
    public static Optional<ItemStack> recoverOriginalSeed(ItemStack geneticSeed) {
        if (!geneticSeed.is(GENETIC_SEED.get()) || !hasGenome(geneticSeed)) return Optional.empty();
        Genome genome = geneticSeed.get(CropComponents.GENOME.get());
        String recorded = geneticSeed.get(CropComponents.ORIGINAL_SEED.get());
        String source = recorded != null ? recorded : originalSeedId(genome).orElse(null);
        if (source == null) return Optional.empty();
        // A saved origin survives removal of an optional integration.
        // However, once a currently active plant definition CLAIMS this item
        // for another species, never silently turn the genome into that item.
        var snapshot = CropGeneticsMod.plants().snapshot();
        if (snapshot.ambiguousSeedItems().contains(source)) return Optional.empty();
        Optional<PlantDefinition> mapped = snapshot.plantForSeed(source);
        if (mapped.isPresent() && !mapped.get().id().equals(genome.plantId())) {
            return Optional.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(source);
        if (id == null) return Optional.empty();
        return BuiltInRegistries.ITEM.getOptional(id)
                .filter(item -> item != Items.AIR && item != GENETIC_SEED.get())
                .map(ItemStack::new);
    }

    /**
     * Mark a seed AFTER an analyzer has obtained its actual genome.
     * Vanilla seed stacks are eligible only if the active crop catalogue
     * maps the seed item unambiguously to the genome's species.
     */
    public static boolean markAnalyzed(ItemStack stack) {
        if (!isGeneticSeedStack(stack)) return false;
        stack.set(CropComponents.ANALYZED.get(), true);
        return true;
    }

    /**
     * Analyze the actual seed in place, without overwriting existing alleles.
     * A plain mapped seed receives the documented baseline 1/1/1 genome,
     * while unsupported or ambiguous seed items remain untouched.
     *
     * Only call on the logical server, after the analyzer action succeeds.
     */
    public static boolean analyzeSeed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (hasGenome(stack)) return markAnalyzed(stack);

        if (stack.is(GENETIC_SEED.get()) || !stack.getComponentsPatch().isEmpty()) {
            return false;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Optional<PlantDefinition> plant = CropGeneticsMod.plants().plantForSeed(itemId);
        if (plant.isEmpty()) return false;

        stack.set(CropComponents.GENOME.get(), Genome.basic(plant.get().id()));
        stack.set(CropComponents.ORIGINAL_SEED.get(), itemId);
        stack.set(CropComponents.ANALYZED.get(), true);
        return true;
    }

    /** Analysis belongs to the individual stack and persists across saves. */
    public static boolean isAnalyzed(ItemStack stack) {
        return GeneticDisclosure.mayShowGenes(
                stack.isEmpty() ? null : stack.get(CropComponents.GENOME.get()),
                Boolean.TRUE.equals(stack.get(CropComponents.ANALYZED.get())));
    }

    /**
     * Client-safe: the full crop species catalogue is server-side only.
     * The renderer/tooltip may read an already synchronized Genome directly.
     */
    public static boolean hasGenome(ItemStack stack) {
        return !stack.isEmpty() && stack.get(CropComponents.GENOME.get()) != null;
    }

    /**
     * Server-authoritative: accepts either our genetic seed or a registered
     * Vanilla/mod seed with an unambiguous species matching its genome.
     */
    public static boolean isGeneticSeedStack(ItemStack stack) {
        if (!hasGenome(stack)) return false;
        Genome genome = stack.get(CropComponents.GENOME.get());
        if (stack.is(GENETIC_SEED.get())) return true;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return CropGeneticsMod.plants().plantForSeed(itemId)
                .map(plant -> plant.id().equals(genome.plantId())).orElse(false);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}

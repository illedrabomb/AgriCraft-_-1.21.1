package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
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

    public static final DeferredItem<Item> CROP_STICKS =
            ITEMS.registerSimpleBlockItem("crop_sticks", CropBlocks.CROP_STICKS);

    /** Creates a genetic seed without losing the inherited genome. */
    public static ItemStack seedWithGenome(Genome genome) {
        ItemStack stack = new ItemStack(GENETIC_SEED.get());
        stack.set(CropComponents.GENOME.get(), genome);
        return stack;
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

    /** Analysis belongs to the individual stack and persists across saves. */
    public static boolean isAnalyzed(ItemStack stack) {
        return hasGenome(stack)
                && Boolean.TRUE.equals(stack.get(CropComponents.ANALYZED.get()));
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

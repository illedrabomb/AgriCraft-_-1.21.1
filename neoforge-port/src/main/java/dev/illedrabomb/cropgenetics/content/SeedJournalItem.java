package dev.illedrabomb.cropgenetics.content;

import net.minecraft.network.chat.Component;
import dev.illedrabomb.cropgenetics.data.MutationDefinition;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.TreeSet;

/**
 * AgriCraft-style personal discovery journal. The discovered species belong
 * to this particular ItemStack, so handing the book to another player also
 * transfers its contents. Ponder is not required.
 */
public final class SeedJournalItem extends Item {
    public SeedJournalItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static List<String> discovered(ItemStack journal) {
        if (!(journal.getItem() instanceof SeedJournalItem)) return List.of();
        String stored = journal.getOrDefault(CropComponents.JOURNAL_DISCOVERIES.get(), "");
        TreeSet<String> unique = new TreeSet<>();
        for (String id : stored.split("\\|")) {
            ResourceLocation resource = ResourceLocation.tryParse(id);
            if (resource != null && id.equals(resource.toString())) unique.add(id);
        }
        return List.copyOf(unique);
    }

    /** Returns true only when this analysis unlocks a new species. */
    public static boolean recordDiscovery(ItemStack journal, String plantId) {
        if (!(journal.getItem() instanceof SeedJournalItem)) return false;
        ResourceLocation parsed = ResourceLocation.tryParse(plantId);
        if (parsed == null || !plantId.equals(parsed.toString())) return false;
        TreeSet<String> all = new TreeSet<>(discovered(journal));
        if (!all.add(plantId)) return false;
        journal.set(CropComponents.JOURNAL_DISCOVERIES.get(), String.join("|", all));
        return true;
    }

    /** Only fully researched triples are exposed, never undiscovered species. */
    public record MutationHint(String parentA, String parentB, String child) {}

    public static List<MutationHint> knownMutations(ItemStack journal) {
        if (!(journal.getItem() instanceof SeedJournalItem)) return List.of();
        Set<String> unlocked = new HashSet<>(discovered(journal));
        return journal.getOrDefault(CropComponents.JOURNAL_MUTATIONS.get(), List.<String>of())
                .stream()
                .map(value -> value.split(">", -1))
                .filter(parts -> parts.length == 3 && unlocked.contains(parts[0])
                        && unlocked.contains(parts[1]) && unlocked.contains(parts[2]))
                .map(parts -> new MutationHint(parts[0], parts[1], parts[2]))
                .toList();
    }

    /** Rebuilds server-approved mutation hints from the active datapack snapshot. */
    public static boolean refreshKnownMutations(ItemStack journal,
                                                  Map<String, MutationDefinition> definitions) {
        if (!(journal.getItem() instanceof SeedJournalItem)) return false;
        Set<String> unlocked = new HashSet<>(discovered(journal));
        List<String> known = definitions.values().stream()
                .filter(def -> unlocked.contains(def.parentA())
                        && unlocked.contains(def.parentB())
                        && unlocked.contains(def.childPlantId()))
                .map(def -> def.parentA() + ">" + def.parentB() + ">" + def.childPlantId())
                .distinct().sorted().toList();
        List<String> previous = journal.getOrDefault(
                CropComponents.JOURNAL_MUTATIONS.get(), List.<String>of());
        if (previous.equals(known)) return false;
        journal.set(CropComponents.JOURNAL_MUTATIONS.get(), known);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack journal = player.getItemInHand(hand);
        if (level.isClientSide) {
            // The menu reads only synchronized journal data. Discovery changes
            // still happen exclusively on the server in the Seed Analyzer.
            dev.illedrabomb.cropgenetics.client.SeedJournalScreen.open(journal);
        }
        return InteractionResultHolder.sidedSuccess(journal, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.cropgenetics.journal_discovered",
                discovered(stack).size()));
    }
}

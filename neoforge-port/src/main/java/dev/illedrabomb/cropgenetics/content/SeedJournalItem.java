package dev.illedrabomb.cropgenetics.content;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
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
        for (String id : stored.split("\\n")) {
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
        journal.set(CropComponents.JOURNAL_DISCOVERIES.get(), String.join("\\n", all));
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack journal = player.getItemInHand(hand);
        if (!level.isClientSide) {
            List<String> plants = discovered(journal);
            player.sendSystemMessage(Component.translatable("message.cropgenetics.journal_title", plants.size()));
            if (plants.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.cropgenetics.journal_empty"));
            } else {
                for (String id : plants) {
                    player.sendSystemMessage(Component.literal("• ").append(CropSpeciesNames.label(id)));
                }
            }
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

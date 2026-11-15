package dev.illedrabomb.cropgenetics.content;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Handheld seed analyzer: main hand tool + offhand seed, right click.
 * No seed is consumed and pre-existing alleles are never replaced.
 */
public final class SeedAnalyzerItem extends Item {
    public SeedAnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(tool);

        ItemStack seed = player.getOffhandItem();
        if (seed.isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(
                    Component.translatable("message.cropgenetics.analyzer_offhand"), true);
            return InteractionResultHolder.fail(tool);
        }

        if (!level.isClientSide) {
            boolean success = CropItems.analyzeSeed(seed);
            player.displayClientMessage(Component.translatable(success
                    ? "message.cropgenetics.analyzer_success"
                    : "message.cropgenetics.analyzer_unsupported"), true);
        }
        return InteractionResultHolder.success(tool);
    }
}

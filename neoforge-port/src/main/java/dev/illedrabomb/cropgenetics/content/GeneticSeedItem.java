package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/**
 * The server-authoritative analysis marker lives on this ItemStack. Detailed
 * genetics are displayed by the client-only Shift tooltip event.
 */
public final class GeneticSeedItem extends Item {
    public GeneticSeedItem(Properties properties) {
        super(properties);
    }

    /**
     * Sneak + use in the air to turn ONE genetic seed back into its original
     * Vanilla/mod item. A missing source item never consumes the genetic seed.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || held.get(CropComponents.GENOME.get()) == null) {
            return InteractionResultHolder.pass(held);
        }
        if (level.isClientSide) return InteractionResultHolder.success(held);

        var recovered = CropItems.recoverOriginalSeed(held);
        if (recovered.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.cropgenetics.seed_recovery_unavailable"), true);
            return InteractionResultHolder.fail(held);
        }

        ItemStack plainSeed = recovered.get();
        if (held.getCount() == 1) {
            player.displayClientMessage(
                    Component.translatable("message.cropgenetics.seed_recovered"), true);
            return InteractionResultHolder.success(plainSeed);
        }

        // For stacks containing more than one genetic seed, leave the other
        // seeds untouched and place the one recovered seed in the inventory.
        held.shrink(1);
        if (!player.addItem(plainSeed)) player.drop(plainSeed, false);
        player.displayClientMessage(
                Component.translatable("message.cropgenetics.seed_recovered"), true);
        return InteractionResultHolder.success(held);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Genome genome = stack.get(CropComponents.GENOME.get());
        if (genome == null) return;
        tooltip.add(Component.translatable("tooltip.cropgenetics.plant", genome.plantId()));
        tooltip.add(Component.translatable(CropItems.isAnalyzed(stack)
                ? "tooltip.cropgenetics.analyzed_hint"
                : "tooltip.cropgenetics.unanalyzed"));
        tooltip.add(Component.translatable("tooltip.cropgenetics.recover_hint"));
    }
}

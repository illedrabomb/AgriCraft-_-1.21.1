package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropSeedSourceSafetyTest {
    @Test
    void plainUnmodifiedSourceIsSafeToConvert() {
        assertTrue(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(
                new ItemStack(Items.WHEAT_SEEDS)));
    }

    @Test
    void customDataOnOrdinarySeedIsNotSilentlyDiscarded() {
        ItemStack renamed = new ItemStack(Items.WHEAT_SEEDS);
        renamed.set(DataComponents.CUSTOM_NAME, Component.literal("unique mod seed"));
        assertFalse(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(renamed));
    }

    @Test
    void customDataOnAlreadyAnalyzedSeedAlsoBlocksLossyConversion() {
        ItemStack analyzed = new ItemStack(Items.WHEAT_SEEDS);
        analyzed.set(DataComponents.CUSTOM_NAME, Component.literal("analyzed and renamed"));
        assertFalse(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(analyzed));
    }

    @Test
    void customDataOnGeneticSeedAlsoBlocksLossyConversion() {
        ItemStack genetic = new ItemStack(Items.PAPER);
        genetic.set(DataComponents.CUSTOM_NAME, Component.literal("genetic and renamed"));
        assertFalse(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(genetic));
    }

    @Test
    void emptySourceNeedsNoComponentTransfer() {
        assertTrue(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(ItemStack.EMPTY));
    }
}

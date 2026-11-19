package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
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
                new ItemStack(Items.WHEAT_SEEDS), null, Items.PAPER));
    }

    @Test
    void customDataOnOrdinarySeedIsNotSilentlyDiscarded() {
        ItemStack renamed = new ItemStack(Items.WHEAT_SEEDS);
        renamed.set(DataComponents.CUSTOM_NAME, Component.literal("unique mod seed"));
        assertFalse(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(
                renamed, null, Items.PAPER));
    }

    @Test
    void AlreadyGenotypedSourceMayKeepItsExistingGenetics() {
        ItemStack analyzed = new ItemStack(Items.WHEAT_SEEDS);
        analyzed.set(DataComponents.CUSTOM_NAME, Component.literal("analyzed"));
        assertTrue(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(
                analyzed, Genome.basic("minecraft:wheat_plant"), Items.PAPER));
    }

    @Test
    void GeneticSeedItselfMayCarryItsOwnComponents() {
        ItemStack genetic = new ItemStack(Items.PAPER);
        genetic.set(DataComponents.CUSTOM_NAME, Component.literal("genetic"));
        assertTrue(CropSeedSourceSafety.canConvertWithoutExternalDataLoss(
                genetic, null, Items.PAPER));
    }
}

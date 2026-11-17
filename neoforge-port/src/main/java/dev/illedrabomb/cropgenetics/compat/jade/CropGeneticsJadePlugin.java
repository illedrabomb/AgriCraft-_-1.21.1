package dev.illedrabomb.cropgenetics.compat.jade;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropSpeciesNames;
import dev.illedrabomb.cropgenetics.content.CropSticksBlock;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Optional Jade look-at integration for crop sticks.
 * Only species and stage are shown. Never expose unanalysed gene values.
 */
@WailaPlugin
public final class CropGeneticsJadePlugin implements IWailaPlugin {
    private static final ResourceLocation CROP_INFO =
            ResourceLocation.fromNamespaceAndPath(CropGeneticsMod.MOD_ID, "crop_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Species and stage are already synchronized by our BlockEntity.
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CropInfoProvider.INSTANCE, CropSticksBlock.class);
    }

    public enum CropInfoProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof CropSticksBlockEntity crop)) return;
            Genome genome = crop.genome();
            if (genome == null) return;
            int total = crop.totalGrowthStages() > 0 ? crop.totalGrowthStages() : 8;
            int stage = Math.max(0, Math.min(crop.growthStage(), total - 1));
            tooltip.add(Component.translatable("overlay.cropgenetics.crop",
                    CropSpeciesNames.label(genome.plantId()), stage, total - 1));
        }

        @Override
        public ResourceLocation getUid() {
            return CROP_INFO;
        }
    }
}

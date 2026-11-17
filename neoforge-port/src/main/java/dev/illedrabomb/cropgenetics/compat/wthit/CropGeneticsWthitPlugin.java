package dev.illedrabomb.cropgenetics.compat.wthit;

import dev.illedrabomb.cropgenetics.content.CropSpeciesNames;
import dev.illedrabomb.cropgenetics.content.CropSticksBlock;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.network.chat.Component;

/** Optional WTHIT API plugin; read only client-synchronized crop display data. */
public final class CropGeneticsWthitPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(CropInfoProvider.INSTANCE, CropSticksBlock.class);
    }

    private enum CropInfoProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof CropSticksBlockEntity crop)) return;
            Genome genome = crop.genome();
            if (genome == null) return;
            int total = crop.totalGrowthStages() > 0 ? crop.totalGrowthStages() : 8;
            int stage = Math.max(0, Math.min(crop.growthStage(), total - 1));
            tooltip.addLine(Component.translatable("overlay.cropgenetics.crop",
                    CropSpeciesNames.label(genome.plantId()), stage, total - 1));
        }
    }
}

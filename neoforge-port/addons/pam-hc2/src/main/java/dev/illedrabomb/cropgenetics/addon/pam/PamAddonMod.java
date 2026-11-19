package dev.illedrabomb.cropgenetics.addon.pam;

import com.google.gson.JsonParser;
import dev.illedrabomb.cropgenetics.data.CropDiscoveryAddon;
import dev.illedrabomb.cropgenetics.data.CropDiscoveryAddonRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
import dev.illedrabomb.cropgenetics.data.ExplicitPlantClaims;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.io.Reader;
import java.util.List;
import java.util.Map;

/** Self-contained Pam's HarvestCraft 2 Crops adapter. */
@Mod(PamAddonMod.MOD_ID)
public final class PamAddonMod implements CropDiscoveryAddon {
    public static final String MOD_ID = "cropgenetics_pam";
    private static final ResourceLocation SETTINGS = ResourceLocation.fromNamespaceAndPath(
            "cropgenetics", "cropgenetics/pam_auto_discovery.json");

    public PamAddonMod(IEventBus bus, ModContainer container) {
        CropDiscoveryAddonRegistry.register(this);
    }

    @Override public String addonId() { return MOD_ID; }

    @Override public String requiredModId() { return "pamhc2crops"; }

    @Override
    public void discover(ResourceManager manager, Map<String, PlantDefinition> plants,
                         ExplicitPlantClaims claims,
                         List<CropReloadReport.Entry> details) {
        // Only the highest-priority datapack resource defines the policy.
        // Invalid settings disable this add-on's fallback, not core crops.
        PamAutoDiscoverySettings policy = PamAutoDiscoverySettings.DEFAULT;
        var config = manager.getResource(SETTINGS);
        if (config.isPresent()) {
            try (Reader reader = config.get().openAsReader()) {
                policy = PamAutoDiscoverySettings.parse(
                        JsonParser.parseReader(reader).getAsJsonObject());
            } catch (Exception ex) {
                details.add(new CropReloadReport.Entry(
                        CropReloadReport.Kind.PLANT, SETTINGS, "",
                        CropReloadReport.Status.INVALID,
                        "Invalid Pam's auto-discovery settings; discovery disabled: "
                                + ex.getMessage()));
                return;
            }
        }
        if (policy.enabled()) {
            PamCropAutoDiscovery.addMissing(
                    plants, claims.species(), claims.seeds(), claims.produce(),
                    details, policy);
        }
    }
}

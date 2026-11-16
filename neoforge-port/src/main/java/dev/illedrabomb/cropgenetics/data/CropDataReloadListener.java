package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Reads the complete plant and mutation data from a single resource manager
 * and publishes an immutable catalogue + per-file diagnostics in one step.
 */
public final class CropDataReloadListener extends
        SimplePreparableReloadListener<CropDataReloadListener.Prepared> {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String PLANTS = "cropgenetics/plants";
    private static final String MUTATIONS = "cropgenetics/mutations";
    private static final ResourceLocation PAM_SETTINGS = ResourceLocation.fromNamespaceAndPath(
            "cropgenetics", "cropgenetics/pam_auto_discovery.json");

    private final AgriPlantsRegistry registry;
    private final Predicate<String> loadedMod;

    public CropDataReloadListener(AgriPlantsRegistry registry, Predicate<String> loadedMod) {
        this.registry = registry;
        this.loadedMod = loadedMod;
    }

    @Override
    protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        List<CropReloadReport.Entry> errors = new ArrayList<>();
        Map<ResourceLocation, JsonElement> plants =
                readJson(manager, PLANTS, CropReloadReport.Kind.PLANT, errors);
        Map<ResourceLocation, JsonElement> mutations =
                readJson(manager, MUTATIONS, CropReloadReport.Kind.MUTATION, errors);
        // Parse only the selected highest-priority server resource. A bad
        // policy fails closed rather than unexpectedly enabling unknown crops.
        PamAutoDiscoverySettings policy = PamAutoDiscoverySettings.DEFAULT;
        var config = manager.getResource(PAM_SETTINGS);
        if (config.isPresent()) {
            try (Reader reader = config.get().openAsReader()) {
                policy = PamAutoDiscoverySettings.parse(
                        JsonParser.parseReader(reader).getAsJsonObject());
            } catch (Exception ex) {
                policy = new PamAutoDiscoverySettings(false, 0, 9, 0.75D,
                        0.025D, 5, Set.of());
                errors.add(entry(CropReloadReport.Kind.PLANT, PAM_SETTINGS,
                        "", CropReloadReport.Status.INVALID,
                        "Invalid auto-discovery settings; discovery disabled: "
                                + ex.getMessage()));
            }
        }
        return new Prepared(plants, mutations, List.copyOf(errors), policy);
    }

    private static Map<ResourceLocation, JsonElement> readJson(
            ResourceManager manager, String root, CropReloadReport.Kind kind,
            List<CropReloadReport.Entry> errors) {
        Map<ResourceLocation, JsonElement> result = new HashMap<>();
        // listResources applies pack priority for each matching resource path.
        manager.listResources(root, id -> id.getPath().startsWith(root + "/")
                        && id.getPath().endsWith(".json"))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    try (Reader reader = entry.getValue().openAsReader()) {
                        result.put(entry.getKey(), JsonParser.parseReader(reader));
                    } catch (Exception ex) {
                        errors.add(new CropReloadReport.Entry(
                                kind, entry.getKey(), "", CropReloadReport.Status.INVALID,
                                "Unreadable JSON: " + ex.getMessage()));
                    }
                });
        return Map.copyOf(result);
    }

    @Override
    protected void apply(Prepared input, ResourceManager manager, ProfilerFiller profiler) {
        List<CropReloadReport.Entry> details = new ArrayList<>(input.readErrors());
        Map<String, PlantDefinition> plants = new HashMap<>();
        // Detect ID collisions before validation. Otherwise an invalid SECOND
        // definition can leave the FIRST active and hide an ambiguous species.
        // Disabled files and files requiring absent mods do not claim IDs.
        Map<String, List<ResourceLocation>> claimants = new HashMap<>();
        input.plants().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(resource -> {
            try {
                JsonObject json = resource.getValue().getAsJsonObject();
                if (!AgriPlantsParser.enabled(json) || missingMod(json) != null
                        || !json.has("id")) return;
                String candidate = json.get("id").getAsString();
                claimants.computeIfAbsent(candidate, ignored -> new ArrayList<>())
                        .add(resource.getKey());
            } catch (RuntimeException ignored) {
                // The full pass below records INVALID, including malformed IDs.
            }
        });
        Set<String> duplicates = new HashSet<>();
        claimants.forEach((id, files) -> {
            if (files.size() > 1) duplicates.add(id);
        });

        input.plants().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(resource -> {
            ResourceLocation key = resource.getKey();
            String id = "";
            try {
                JsonObject json = resource.getValue().getAsJsonObject();
                id = json.has("id") ? json.get("id").getAsString() : "";
                if (!AgriPlantsParser.enabled(json)) {
                    details.add(entry(CropReloadReport.Kind.PLANT, key, id,
                            CropReloadReport.Status.DISABLED, "enabled=false"));
                    return;
                }
                String missing = missingMod(json);
                if (missing != null) {
                    details.add(entry(CropReloadReport.Kind.PLANT, key, id,
                            CropReloadReport.Status.MISSING_MOD, "Missing mod: " + missing));
                    return;
                }
                if (duplicates.contains(id)) {
                    details.add(entry(CropReloadReport.Kind.PLANT, key, id,
                            CropReloadReport.Status.CONFLICT,
                            "Duplicate plant ID: " + id + " in " + claimants.get(id)));
                    return;
                }
                PlantDefinition plant = AgriPlantsParser.plant(json);
                id = plant.id();
                // The apply phase runs with live Minecraft registries available.
                // Reject unknown items/blocks instead of silently growing
                // unharvestable plants or accepting impossible soil rules.
                List<String> registryErrors = PlantRegistryValidator.errors(plant);
                if (!registryErrors.isEmpty()) {
                    details.add(entry(CropReloadReport.Kind.PLANT, key, id,
                            CropReloadReport.Status.INVALID,
                            String.join("; ", registryErrors)));
                    return;
                }
                // Complete diagnostics before staging an active definition.
                List<String> tagWarnings = PlantRegistryValidator.tagWarnings(plant, manager);
                // All checks succeeded. Conflicting IDs were already
                // excluded in the prepass; insertion is now deterministic.
                plants.put(id, plant);
                details.add(entry(CropReloadReport.Kind.PLANT, key, id,
                        tagWarnings.isEmpty() ? CropReloadReport.Status.LOADED
                                : CropReloadReport.Status.WARNING,
                        String.join("; ", tagWarnings)));
            } catch (RuntimeException ex) {
                details.add(entry(CropReloadReport.Kind.PLANT, key, id,
                        CropReloadReport.Status.INVALID, ex.getMessage()));
            }
        });

        // If any explicit plant resource could not be read, its species/seed/
        // harvest claims are unknown. Fail closed for automatic discovery
        // rather than silently replacing an administrator's broken override.
        // Explicitly valid plants and mutations still load as usual.
        boolean unsafeAutoClaims = input.readErrors().stream().anyMatch(error ->
                error.kind() == CropReloadReport.Kind.PLANT
                        && error.source().getPath().startsWith(PLANTS + "/"));
        if (loadedMod.test("pamhc2crops") && input.pamSettings().enabled()) {
            Set<String> explicitSpecies = new HashSet<>();
            Set<String> explicitSeedClaims = new HashSet<>();
            Set<String> explicitProduceClaims = new HashSet<>();
            for (Map.Entry<ResourceLocation, JsonElement> resource : input.plants().entrySet()) {
                try {
                    JsonObject json = resource.getValue().getAsJsonObject();
                    // Every explicit plant must identify its own species.
                    // Without an ID, we cannot guarantee it does not claim
                    // an automatically inferred Pam's species.
                    if (!json.has("id") || !json.get("id").isJsonPrimitive()) {
                        unsafeAutoClaims = true;
                        continue;
                    }
                    explicitSpecies.add(json.get("id").getAsString());
                    if (json.has("seed_items")) {
                        for (JsonElement seed : json.getAsJsonArray("seed_items")) {
                            JsonObject object = seed.getAsJsonObject();
                            if (!object.has("object") || !object.get("object").isJsonPrimitive()) {
                                unsafeAutoClaims = true;
                                continue;
                            }
                            explicitSeedClaims.add(object.get("object").getAsString());
                        }
                    }
                    if (json.has("products")) {
                        JsonObject products = json.getAsJsonObject("products");
                        if (!products.has("products") || !products.get("products").isJsonArray()) {
                            unsafeAutoClaims = true;
                            continue;
                        }
                        for (JsonElement product : products.getAsJsonArray("products")) {
                            JsonObject object = product.getAsJsonObject();
                            if (!object.has("object") || !object.get("object").isJsonPrimitive()) {
                                unsafeAutoClaims = true;
                                continue;
                            }
                            explicitProduceClaims.add(object.get("object").getAsString());
                        }
                    }
                } catch (RuntimeException ignored) {
                    // Malformed claims are not safe to infer or replace.
                    unsafeAutoClaims = true;
                }
            }
            if (unsafeAutoClaims) {
                details.add(entry(CropReloadReport.Kind.PLANT, PAM_SETTINGS, "",
                        CropReloadReport.Status.WARNING,
                        "Pam's automatic discovery disabled for this reload: "
                                + "unreadable/malformed explicit plant identity or item claims"));
            } else {
                PamCropAutoDiscovery.addMissing(
                        plants, explicitSpecies, explicitSeedClaims, explicitProduceClaims,
                        details, input.pamSettings());
            }
        }

        Map<String, MutationDefinition> mutations = new HashMap<>();
        input.mutations().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(resource -> {
            ResourceLocation key = resource.getKey();
            try {
                JsonObject json = resource.getValue().getAsJsonObject();
                if (!AgriPlantsParser.enabled(json)) {
                    details.add(entry(CropReloadReport.Kind.MUTATION, key, "",
                            CropReloadReport.Status.DISABLED, "enabled=false"));
                    return;
                }
                String missing = missingMod(json);
                if (missing != null) {
                    details.add(entry(CropReloadReport.Kind.MUTATION, key, "",
                            CropReloadReport.Status.MISSING_MOD, "Missing mod: " + missing));
                    return;
                }
                MutationDefinition mutation = AgriPlantsParser.mutation(json);
                if (mutation.environment().fluidId().isPresent()) {
                    String fluidId = mutation.environment().fluidId().get();
                    ResourceLocation fluidKey = ResourceLocation.tryParse(fluidId);
                    if (fluidKey == null || BuiltInRegistries.FLUID.getOptional(fluidKey).isEmpty()) {
                        details.add(entry(CropReloadReport.Kind.MUTATION, key, mutation.childPlantId(),
                                CropReloadReport.Status.INVALID, "Unknown mutation fluid: " + fluidId));
                        return;
                    }
                }
                List<String> missingSpecies = new ArrayList<>();
                if (!plants.containsKey(mutation.parentA())) missingSpecies.add(mutation.parentA());
                if (!plants.containsKey(mutation.parentB())) missingSpecies.add(mutation.parentB());
                if (!plants.containsKey(mutation.childPlantId())) missingSpecies.add(mutation.childPlantId());
                if (!missingSpecies.isEmpty()) {
                    details.add(entry(CropReloadReport.Kind.MUTATION, key,
                            mutation.childPlantId(), CropReloadReport.Status.MISSING_SPECIES,
                            "Unknown or disabled species: " + String.join(", ", missingSpecies)));
                    return;
                }
                mutations.put(key.toString(), mutation);
                details.add(entry(CropReloadReport.Kind.MUTATION, key, mutation.childPlantId(),
                        CropReloadReport.Status.LOADED, ""));
            } catch (RuntimeException ex) {
                details.add(entry(CropReloadReport.Kind.MUTATION, key, "",
                        CropReloadReport.Status.INVALID, ex.getMessage()));
            }
        });

        CropReloadReport report = new CropReloadReport(details);
        registry.replaceAll(plants, mutations, report);
        AgriPlantsRegistry.Snapshot active = registry.snapshot();

        LOG.info("Crop datapack reload: {} plants, {} mutations, {} resources ({})",
                active.plants().size(), active.mutations().size(), report.entries().size(),
                report.totals());
        for (CropReloadReport.Entry problem : report.entries()) {
            if (problem.status() != CropReloadReport.Status.LOADED) {
                LOG.warn("Crop data {} {} [{}]: {}",
                        problem.kind(), problem.source(), problem.status(), problem.detail());
            }
        }
        if (!active.ambiguousSeedItems().isEmpty())
            LOG.warn("Ambiguous seed mappings disabled: {}", active.ambiguousSeedItems());
    }

    private static CropReloadReport.Entry entry(
            CropReloadReport.Kind kind, ResourceLocation key, String id,
            CropReloadReport.Status status, String reason) {
        return new CropReloadReport.Entry(kind, key, id, status, reason);
    }

    /** Returns the first unmet dependency, or null if all requirements exist. */
    private String missingMod(JsonObject json) {
        if (!json.has("mods")) return null;
        for (JsonElement element : json.getAsJsonArray("mods")) {
            String modId = element.getAsString();
            if (!"agricraft".equals(modId) && !loadedMod.test(modId)) return modId;
        }
        return null;
    }

    public record Prepared(Map<ResourceLocation, JsonElement> plants,
                           Map<ResourceLocation, JsonElement> mutations,
                           List<CropReloadReport.Entry> readErrors,
                           PamAutoDiscoverySettings pamSettings) {}
}

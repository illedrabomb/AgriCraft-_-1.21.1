package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Server-authoritative explanation of a rejected seed-on-sticks interaction. */
final class CropPlantingFeedback {
    private CropPlantingFeedback() {}

    /** Only registered seed candidates trigger feedback, never arbitrary held items. */
    static void showIfSeed(ServerLevel level, BlockPos pos, BlockState state,
                           CropSticksBlockEntity crop, ItemStack held, Player player) {
        if (!held.is(CropItems.GENETIC_SEED.get())
                && !CropGeneticsSeedRecognition.isRegisteredSeed(held)) return;
        // Called only after the normal planting path declined the action.
        // A protection hook can reject the final state update even when all
        // environmental rules pass; never leave that rejection unexplained.
        String reason = reason(level, pos, state, crop, held);
        if ("soil".equals(reason)) {
            PlantDefinition plant = plantForSeed(held);
            if (plant != null) {
                player.displayClientMessage(Component.translatable(
                        "message.cropgenetics.planting.soil",
                        requiredSoil(plant.requirements())), true);
                return;
            }
        }
        player.displayClientMessage(Component.translatable(
                "message.cropgenetics.planting." + (reason == null ? "blocked" : reason)), true);
    }

    /** A tag is a rule, not a promise that one example block is the only soil. */
    static String requiredSoil(PlantRequirements requirements) {
        return requirements.soilBlock().orElseGet(
                () -> "#" + requirements.soilTag().orElse("?"));
    }

    private static PlantDefinition plantForSeed(ItemStack seed) {
        Genome genome = seed.get(CropComponents.GENOME.get());
        if (genome != null) return CropGeneticsMod.plants().plants().get(genome.plantId());
        String seedId = BuiltInRegistries.ITEM.getKey(seed.getItem()).toString();
        return CropGeneticsMod.plants().plantForSeed(seedId).orElse(null);
    }

    static String reason(ServerLevel level, BlockPos pos, BlockState state,
                         CropSticksBlockEntity crop, ItemStack held) {
        if (state.getValue(CropSticksBlock.CROSS)) return "cross";
        if (crop.genome() != null || state.getValue(CropSticksBlock.PLANTED)) return "occupied";
        if (!CropSeedSourceSafety.canConvertWithoutExternalDataLoss(held)) return "modified";

        Genome genome;
        PlantDefinition plant;
        if (held.is(CropItems.GENETIC_SEED.get())) {
            genome = held.get(CropComponents.GENOME.get());
            plant = genome == null ? null : CropGeneticsMod.plants().plants().get(genome.plantId());
        } else if (CropItems.hasGenome(held)) {
            if (!CropItems.isGeneticSeedStack(held)) return "unknown";
            genome = held.get(CropComponents.GENOME.get());
            plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        } else {
            if (!CropGeneticsSeedRecognition.isUnmodifiedRegisteredSeed(held)) return "modified";
            String seedId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
            plant = CropGeneticsMod.plants().plantForSeed(seedId).orElse(null);
            genome = plant == null ? null : Genome.basic(plant.id());
        }
        if (plant == null || genome == null) return "unknown";

        PlantRequirements requirements = plant.requirements();
        if (!CropConditions.matchesSoil(level, pos, requirements)) return "soil";
        if (!CropConditions.matchesPlantingSite(level, pos, plant)) {
            return switch (plant.id()) {
                case "minecraft:sugar_cane_plant" -> "water";
                case "minecraft:brown_mushroom_plant", "minecraft:red_mushroom_plant" -> "mushroom_light";
                case "minecraft:cactus_plant" -> "cactus_space";
                default -> "environment";
            };
        }
        if (!CropConditions.matchesLocation(level, pos, requirements)) return "location";
        if (!CropConditions.matchesEnvironment(level, pos, requirements.environment())) return "environment";
        if (!CropConditions.matchesSeason(level, pos,
                requirements.seasonRule().allowedSeasons())) return "season";
        if (!CropConditions.matchesChemistry(level, pos, requirements.soilChemistry(),
                genome.strength())) return "chemistry";

        int light = level.getRawBrightness(pos, 0);
        int deficit = Math.max(0, Math.max(requirements.minLight() - light,
                light - requirements.maxLight()));
        int tolerance = requirements.lightTolerance() * (genome.strength() - 1) / 9;
        if (deficit > tolerance) return light < requirements.minLight() ? "dark" : "bright";
        // E.g. a soil profile may disable growth even at otherwise valid light.
        return CropConditions.canGrow(level, pos, plant, genome) ? null : "environment";
    }
}

package dev.illedrabomb.cropgenetics.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import dev.illedrabomb.cropgenetics.data.SoilChemistryRule;
import dev.illedrabomb.cropgenetics.data.SoilOverrideSavedData;
import dev.illedrabomb.cropgenetics.data.SoilProfileRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.Optional;

/**
 * Level-2 administrative soil tools. Commands target the supporting soil block
 * directly, not the crop above it. No auto-generated Vanilla chemistry.
 */
public final class SoilCommand {
    private SoilCommand() {}

    public static void register(RegisterCommandsEvent event, SoilProfileRegistry profiles) {
        event.getDispatcher().register(Commands.literal("cropgenetics")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("soil")
                        .then(Commands.literal("show")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> show(ctx.getSource(),
                                                BlockPosArgument.getBlockPos(ctx, "pos"), profiles))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> clear(ctx.getSource(),
                                                BlockPosArgument.getBlockPos(ctx, "pos")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("humidity", StringArgumentType.word())
                                                .then(Commands.argument("acidity", StringArgumentType.word())
                                                        .then(Commands.argument("nutrients", StringArgumentType.word())
                                                                .executes(ctx -> set(ctx.getSource(),
                                                                        BlockPosArgument.getBlockPos(ctx, "pos"),
                                                                        StringArgumentType.getString(ctx, "humidity"),
                                                                        StringArgumentType.getString(ctx, "acidity"),
                                                                        StringArgumentType.getString(ctx, "nutrients"))))))))));
    }

    private static boolean validTarget(CommandSourceStack source, BlockPos soilPos) {
        ServerLevel level = source.getLevel();
        if (!level.hasChunkAt(soilPos) || !level.isInWorldBounds(soilPos)) {
            reply(source, "Soil position is outside world bounds or its chunk is not loaded.");
            return false;
        }
        if (level.getBlockState(soilPos).is(Blocks.AIR)) {
            reply(source, "Cannot assign a soil profile to air.");
            return false;
        }
        return true;
    }

    private static int show(CommandSourceStack source, BlockPos pos, SoilProfileRegistry profiles) {
        if (!validTarget(source, pos)) return 0;
        ServerLevel level = source.getLevel();
        Optional<SoilChemistryAccess.Sample> override = SoilOverrideSavedData.get(level).sample(pos);
        Optional<SoilChemistryAccess.Sample> profile =
                profiles.sample(level.getBlockState(pos));
        Optional<SoilChemistryAccess.Sample> effective = override.isPresent() ? override : profile;
        if (effective.isEmpty()) {
            reply(source, "No soil sample at " + pos.toShortString()
                    + " (no saved override or datapack profile).");
            return 0;
        }
        SoilChemistryAccess.Sample sample = effective.get();
        reply(source, "Soil " + pos.toShortString() + " ["
                + (override.isPresent() ? "saved override" : "datapack profile") + "]: "
                + sample.humidity().name().toLowerCase(Locale.ROOT) + ", "
                + sample.acidity().name().toLowerCase(Locale.ROOT) + ", "
                + sample.nutrients().name().toLowerCase(Locale.ROOT));
        return 1;
    }

    private static int set(CommandSourceStack source, BlockPos pos, String humidity,
                           String acidity, String nutrients) {
        if (!validTarget(source, pos)) return 0;
        try {
            SoilChemistryAccess.Sample sample = new SoilChemistryAccess.Sample(
                    SoilChemistryRule.parse(SoilChemistryRule.Humidity.class, humidity),
                    SoilChemistryRule.parse(SoilChemistryRule.Acidity.class, acidity),
                    SoilChemistryRule.parse(SoilChemistryRule.Nutrients.class, nutrients));
            if (!SoilOverrideSavedData.get(source.getLevel()).put(pos, sample)) {
                reply(source, "Too many saved soil overrides in this dimension (limit 65536).");
                return 0;
            }
            reply(source, "Saved soil override for " + pos.toShortString()
                    + "; values are dimension-specific and survive restarts.");
            return 1;
        } catch (IllegalArgumentException ex) {
            reply(source, "Invalid soil category: " + ex.getMessage());
            return 0;
        }
    }

    private static int clear(CommandSourceStack source, BlockPos pos) {
        if (!validTarget(source, pos)) return 0;
        if (SoilOverrideSavedData.get(source.getLevel()).remove(pos)) {
            reply(source, "Cleared soil override at " + pos.toShortString()
                    + "; any static datapack profile applies again.");
            return 1;
        }
        reply(source, "No saved soil override at " + pos.toShortString() + ".");
        return 0;
    }

    private static void reply(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }
}

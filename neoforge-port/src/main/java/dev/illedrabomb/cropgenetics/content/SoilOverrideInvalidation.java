package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.SoilOverrideSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Conservative server-side reconciliation after ordinary player block edits.
 * NeoForge break/place events are cancellable and occur before final outcome;
 * only reconcile at end of the level tick, after other listeners have run.
 * Vanilla commands, pistons, explosions, mod machines and same-type breaks
 * without a reported place event are NOT covered by these hooks.
 */
public final class SoilOverrideInvalidation {
    private SoilOverrideInvalidation() {}
    private static final int MAX_PENDING_PER_LEVEL = 8192;

    private record Pending(BlockPos pos, BlockState original, BlockState intended,
                           BooleanSupplier accepted) {}

    private static final Map<ServerLevel, List<Pending>> pending = new HashMap<>();

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        enqueue(level, new Pending(event.getPos().immutable(), event.getState(),
                null, () -> !event.isCanceled()));
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || event instanceof BlockEvent.EntityMultiPlaceEvent) return;
        enqueue(level, new Pending(event.getPos().immutable(),
                event.getBlockSnapshot().getState(), event.getPlacedBlock(),
                () -> !event.isCanceled()));
    }

    public static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (var snapshot : event.getReplacedBlockSnapshots()) {
            enqueue(level, new Pending(snapshot.getPos().immutable(), snapshot.getState(),
                    null, () -> !event.isCanceled()));
        }
    }

    private static void enqueue(ServerLevel level, Pending entry) {
        List<Pending> entries = pending.computeIfAbsent(level, unused -> new ArrayList<>());
        // Protect dedicated servers from huge queues; lazy block-ID validation
        // still handles many changes even when this queue reaches its cap.
        if (entries.size() < MAX_PENDING_PER_LEVEL) entries.add(entry);
    }

    public static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<Pending> entries = pending.remove(level);
        if (entries == null || entries.isEmpty()) return;
        SoilOverrideSavedData data = null;
        for (Pending entry : entries) {
            if (!entry.accepted().getAsBoolean() || !level.hasChunkAt(entry.pos()))
                continue;
            BlockState current = level.getBlockState(entry.pos());
            // For a successful placement, the final block must match the
            // declared placement. For a break, the old block must be gone.
            boolean changed = entry.intended() != null
                    ? current.is(entry.intended().getBlock())
                    : !current.is(entry.original().getBlock());
            if (!changed) continue;
            if (data == null) data = SoilOverrideSavedData.get(level);
            data.remove(entry.pos());
        }
    }
}

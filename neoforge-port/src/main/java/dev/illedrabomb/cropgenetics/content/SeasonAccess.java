package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stable boundary for optional season integrations.
 * No built-in calendar, no runtime dependency on a seasons mod.
 * Install an adapter only after its mod/API has been verified.
 */
public final class SeasonAccess {
    private SeasonAccess() {}

    @FunctionalInterface
    public interface Provider {
        /** Empty means unknown (not necessarily no season system installed). */
        Optional<String> currentSeason(ServerLevel level, BlockPos pos);
    }

    private static volatile Provider provider;

    public static void install(Provider implementation) {
        provider = Objects.requireNonNull(implementation);
    }

    public static void uninstall() {
        provider = null;
    }

    public static boolean matches(ServerLevel level, BlockPos pos, List<String> allowedSeasons) {
        if (allowedSeasons.isEmpty()) return true;
        Provider active = provider;
        if (active == null) return true; // No seasons mod: ignore seasonal limits.
        Optional<String> season = active.currentSeason(level, pos);
        // An installed provider that cannot determine a season must not
        // silently override configured restrictions.
        return season != null && season.filter(allowedSeasons::contains).isPresent();
    }

    /** Client-side checks cannot use a server-only seasons provider. */
    public static boolean matchesClient(List<String> allowedSeasons) {
        // Server ultimately authorizes planting/growth and mutation results.
        return true;
    }
}

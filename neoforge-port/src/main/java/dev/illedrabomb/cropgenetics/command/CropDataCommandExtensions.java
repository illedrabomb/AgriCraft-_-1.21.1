package dev.illedrabomb.cropgenetics.command;

import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Admin diagnostic subcommands contributed by installed addon entrypoints.
 * The core has no compile-time reference to Pam or Farmer command classes.
 */
public final class CropDataCommandExtensions {
    @FunctionalInterface
    public interface Handler {
        int execute(CommandSourceStack source, AgriPlantsRegistry registry);
    }

    public record Extension(String name, Handler handler) {}

    private static final List<Extension> ENTRIES = new ArrayList<>();

    private CropDataCommandExtensions() {}

    public static synchronized void register(String name, Handler handler) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(handler);
        if (!name.matches("[a-z][a-z0-9_]*"))
            throw new IllegalArgumentException("Invalid crop addon command");
        if (ENTRIES.stream().anyMatch(entry -> entry.name().equals(name)))
            throw new IllegalArgumentException("Duplicate crop addon command: " + name);
        ENTRIES.add(new Extension(name, handler));
        ENTRIES.sort(Comparator.comparing(Extension::name));
    }

    public static synchronized List<Extension> registered() {
        return List.copyOf(ENTRIES);
    }
}

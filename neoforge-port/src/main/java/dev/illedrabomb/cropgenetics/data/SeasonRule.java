package dev.illedrabomb.cropgenetics.data;

import java.util.List;

/** Optional seasonal constraint. No season provider means no seasonal penalty. */
public record SeasonRule(List<String> allowedSeasons) {
    public static final SeasonRule UNRESTRICTED = new SeasonRule(List.of());
    public SeasonRule {
        allowedSeasons = List.copyOf(allowedSeasons);
        if (allowedSeasons.size() > 4 || allowedSeasons.stream().distinct().count() != allowedSeasons.size())
            throw new IllegalArgumentException("Expected at most four distinct seasons");
        for (String season : allowedSeasons) {
            if (!List.of("spring", "summer", "autumn", "winter").contains(season))
                throw new IllegalArgumentException("Unsupported season: " + season);
        }
    }
}

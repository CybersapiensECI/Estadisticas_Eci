package com.cybersapiens.estadisticaseci.infrastructure.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GamificationUserMonasResponse(
        String userId,
        int totalXp,
        int totalUnlocked,
        List<MonaEntry> unlocked,
        List<MonaProgressEntry> inProgress,
        List<MonaEntry> locked
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MonaEntry(
            String code,
            String name,
            String rarity,
            String imageUrl,
            int xpGranted,
            String unlockedAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MonaProgressEntry(
            String code,
            String name,
            String rarity,
            String imageUrl,
            int currentCount,
            int requiredCount,
            int progressPercentage
    ) {}
}

package com.cybersapiens.estadisticaseci.domain.model;

import java.util.List;

public record UserPersonalStats(
        String userId,
        GamificationStats gamification,
        EventStats events,
        ParcheStats parches,
        ProfileStats profile
) {
    public record GamificationStats(
            int totalXp,
            int totalMonasUnlocked,
            int monasInProgress,
            int monasLocked,
            double completionPercentage,
            List<MonaEntry> monas
    ) {}

    public record MonaEntry(
            String code,
            String name,
            String rarity,
            String status,
            String unlockedAt
    ) {}

    public record EventStats(
            int totalAttended,
            int upcomingEvents,
            int totalEvents,
            List<String> eventIds
    ) {}

    public record ParcheStats(
            int totalJoined,
            int activeParches
    ) {}

    public record ProfileStats(
            int xp,
            int level,
            boolean isActive,
            String career,
            Integer semester
    ) {}
}

package com.cybersapiens.estadisticaseci.infrastructure.web.client.dto;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;

import java.util.List;

public record UserPersonalStatsResponse(
        String userId,
        GamificationStatsResponse gamification,
        EventStatsResponse events,
        ParcheStatsResponse parches,
        ProfileStatsResponse profile
) {
    public record GamificationStatsResponse(
            int totalXp,
            int totalMonasUnlocked,
            int monasInProgress,
            int monasLocked,
            double completionPercentage,
            List<MonaEntryResponse> monas
    ) {}

    public record MonaEntryResponse(
            String code,
            String name,
            String rarity,
            String status,
            String unlockedAt
    ) {}

    public record EventStatsResponse(
            int totalAttended,
            int upcomingEvents,
            int totalEvents,
            List<String> eventIds
    ) {}

    public record ParcheStatsResponse(
            int totalJoined,
            int activeParches
    ) {}

    public record ProfileStatsResponse(
            int xp,
            int level,
            boolean isActive,
            String career,
            Integer semester
    ) {}

    public static UserPersonalStatsResponse fromDomain(UserPersonalStats stats) {
        return new UserPersonalStatsResponse(
                stats.userId(),
                stats.gamification() != null ? new GamificationStatsResponse(
                        stats.gamification().totalXp(),
                        stats.gamification().totalMonasUnlocked(),
                        stats.gamification().monasInProgress(),
                        stats.gamification().monasLocked(),
                        stats.gamification().completionPercentage(),
                        stats.gamification().monas().stream()
                                .map(m -> new MonaEntryResponse(m.code(), m.name(), m.rarity(), m.status(), m.unlockedAt()))
                                .toList()
                ) : null,
                stats.events() != null ? new EventStatsResponse(
                        stats.events().totalAttended(),
                        stats.events().upcomingEvents(),
                        stats.events().totalEvents(),
                        stats.events().eventIds()
                ) : null,
                stats.parches() != null ? new ParcheStatsResponse(
                        stats.parches().totalJoined(),
                        stats.parches().activeParches()
                ) : null,
                stats.profile() != null ? new ProfileStatsResponse(
                        stats.profile().xp(),
                        stats.profile().level(),
                        stats.profile().isActive(),
                        stats.profile().career(),
                        stats.profile().semester()
                ) : null
        );
    }
}

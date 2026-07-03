package com.cybersapiens.estadisticaseci.infrastructure.web.client;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalGamificationPort;
import com.cybersapiens.estadisticaseci.infrastructure.web.client.dto.GamificationUserMonasResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class GamificationServiceClient implements ExternalGamificationPort {

    private final WebClient webClient;

    public GamificationServiceClient(WebClient gamificationWebClient) {
        this.webClient = gamificationWebClient;
    }

    @Override
    public UserPersonalStats.GamificationStats getGamificationStats(String userId) {
        GamificationUserMonasResponse response = webClient.get()
                .uri("/api/v1/gamification/users/{userId}/monas", userId)
                .retrieve()
                .bodyToMono(GamificationUserMonasResponse.class)
                .block();

        if (response == null) {
            return emptyGamificationStats();
        }

        List<UserPersonalStats.MonaEntry> monas = new ArrayList<>();

        if (response.unlocked() != null) {
            response.unlocked().forEach(m -> monas.add(new UserPersonalStats.MonaEntry(
                    m.code(), m.name(), m.rarity(), "UNLOCKED", m.unlockedAt()
            )));
        }
        if (response.inProgress() != null) {
            response.inProgress().forEach(m -> monas.add(new UserPersonalStats.MonaEntry(
                    m.code(), m.name(), m.rarity(), "IN_PROGRESS", null
            )));
        }
        if (response.locked() != null) {
            response.locked().forEach(m -> monas.add(new UserPersonalStats.MonaEntry(
                    m.code(), m.name(), m.rarity(), "LOCKED", null
            )));
        }

        int total = response.totalUnlocked() + (response.inProgress() != null ? response.inProgress().size() : 0)
                + (response.locked() != null ? response.locked().size() : 0);

        double completion = total > 0 ? (double) response.totalUnlocked() / total * 100 : 0;

        return new UserPersonalStats.GamificationStats(
                response.totalXp(),
                response.totalUnlocked(),
                response.inProgress() != null ? response.inProgress().size() : 0,
                response.locked() != null ? response.locked().size() : 0,
                Math.round(completion * 100.0) / 100.0,
                monas
        );
    }

    private UserPersonalStats.GamificationStats emptyGamificationStats() {
        return new UserPersonalStats.GamificationStats(0, 0, 0, 0, 0, List.of());
    }
}

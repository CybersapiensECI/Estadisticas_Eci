package com.cybersapiens.estadisticaseci.infrastructure.web.client;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalProfilePort;
import com.cybersapiens.estadisticaseci.infrastructure.web.client.dto.ProfileUserResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ProfileServiceClient implements ExternalProfilePort {

    private final WebClient webClient;

    public ProfileServiceClient(WebClient profileWebClient) {
        this.webClient = profileWebClient;
    }

    @Override
    public UserPersonalStats.ProfileStats getProfileStats(String userId) {
        ProfileUserResponse response = webClient.get()
                .uri("/api/v1/users/{userId}", userId)
                .retrieve()
                .bodyToMono(ProfileUserResponse.class)
                .block();

        if (response == null) {
            return new UserPersonalStats.ProfileStats(0, 1, false, null, null);
        }

        return new UserPersonalStats.ProfileStats(
                response.xp() != null ? response.xp() : 0,
                response.level() != null ? response.level() : 1,
                response.active() != null ? response.active() : false,
                response.career(),
                response.semester()
        );
    }
}

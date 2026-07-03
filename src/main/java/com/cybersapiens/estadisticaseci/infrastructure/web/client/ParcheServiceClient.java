package com.cybersapiens.estadisticaseci.infrastructure.web.client;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalParchePort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class ParcheServiceClient implements ExternalParchePort {

    private final WebClient webClient;

    public ParcheServiceClient(WebClient parcheWebClient) {
        this.webClient = parcheWebClient;
    }

    @Override
    public UserPersonalStats.ParcheStats getParcheStats(String userId) {
        List<Map<String, Object>> parches = webClient.get()
                .uri("/api/parches/user/{userId}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        if (parches == null || parches.isEmpty()) {
            return new UserPersonalStats.ParcheStats(0, 0);
        }

        long active = parches.stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase((String) p.get("status")))
                .count();

        return new UserPersonalStats.ParcheStats(parches.size(), (int) active);
    }
}

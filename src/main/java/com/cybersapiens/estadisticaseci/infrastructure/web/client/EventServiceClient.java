package com.cybersapiens.estadisticaseci.infrastructure.web.client;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalEventPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class EventServiceClient implements ExternalEventPort {

    private final WebClient webClient;

    public EventServiceClient(WebClient eventWebClient) {
        this.webClient = eventWebClient;
    }

    @Override
    public UserPersonalStats.EventStats getEventStats(String userId) {
        List<String> eventIds = webClient.get()
                .uri("/events/agenda?userId={userId}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block();

        if (eventIds == null || eventIds.isEmpty()) {
            return new UserPersonalStats.EventStats(0, 0, 0, List.of());
        }

        return new UserPersonalStats.EventStats(
                eventIds.size(),
                0,
                eventIds.size(),
                eventIds
        );
    }
}

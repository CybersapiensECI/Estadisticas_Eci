package com.cybersapiens.estadisticaseci.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient gamificationWebClient(WebClient.Builder builder,
                                           @Value("${services.gamification.url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean
    public WebClient eventWebClient(WebClient.Builder builder,
                                    @Value("${services.event.url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean
    public WebClient parcheWebClient(WebClient.Builder builder,
                                     @Value("${services.parches.url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean
    public WebClient profileWebClient(WebClient.Builder builder,
                                      @Value("${services.profile.url}") String url) {
        return builder.baseUrl(url).build();
    }
}

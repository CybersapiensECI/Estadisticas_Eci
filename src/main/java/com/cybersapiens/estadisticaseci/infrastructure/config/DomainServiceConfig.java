package com.cybersapiens.estadisticaseci.infrastructure.config;

import com.cybersapiens.estadisticaseci.application.handler.GetIntegrationMetricsHandler;
import com.cybersapiens.estadisticaseci.application.handler.GetUserPersonalStatsHandler;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.domain.port.in.GetUserPersonalStatsUseCase;
import com.cybersapiens.estadisticaseci.domain.port.out.ActivityDataPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalGamificationPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalEventPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalParchePort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalProfilePort;
import com.cybersapiens.estadisticaseci.domain.port.out.MentorshipDataPort;
import com.cybersapiens.estadisticaseci.domain.port.out.WelfareDataPort;
import com.cybersapiens.estadisticaseci.domain.service.AnonymizationService;
import com.cybersapiens.estadisticaseci.domain.service.MetricsCalculationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public MetricsCalculationService metricsCalculationService(ActivityDataPort activityDataPort,
                                                               MentorshipDataPort mentorshipDataPort,
                                                               WelfareDataPort welfareDataPort) {
        return new MetricsCalculationService(activityDataPort, mentorshipDataPort, welfareDataPort);
    }

    @Bean
    public AnonymizationService anonymizationService() {
        return new AnonymizationService();
    }

    @Bean
    public GetIntegrationMetricsUseCase getIntegrationMetricsUseCase(MetricsCalculationService calculationService,
                                                                     AnonymizationService anonymizationService) {
        return new GetIntegrationMetricsHandler(calculationService, anonymizationService);
    }

    @Bean
    public GetUserPersonalStatsUseCase getUserPersonalStatsUseCase(ExternalGamificationPort gamificationPort,
                                                                   ExternalEventPort eventPort,
                                                                   ExternalParchePort parchePort,
                                                                   ExternalProfilePort profilePort) {
        return new GetUserPersonalStatsHandler(gamificationPort, eventPort, parchePort, profilePort);
    }
}

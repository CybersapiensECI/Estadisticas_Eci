package com.cybersapiens.estadisticaseci.infrastructure.config;

import com.cybersapiens.estadisticaseci.application.handler.GetIntegrationMetricsHandler;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.domain.port.out.ActivityDataPort;
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
}

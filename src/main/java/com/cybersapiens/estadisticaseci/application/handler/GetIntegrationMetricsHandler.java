package com.cybersapiens.estadisticaseci.application.handler;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.IntegrationMetrics;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.domain.service.AnonymizationService;
import com.cybersapiens.estadisticaseci.domain.service.MetricsCalculationService;
import com.cybersapiens.estadisticaseci.shared.exception.NoDataFoundException;

public class GetIntegrationMetricsHandler implements GetIntegrationMetricsUseCase {

    private final MetricsCalculationService calculationService;
    private final AnonymizationService anonymizationService;

    public GetIntegrationMetricsHandler(MetricsCalculationService calculationService,
                                        AnonymizationService anonymizationService) {
        this.calculationService = calculationService;
        this.anonymizationService = anonymizationService;
    }

    @Override
    public IntegrationMetrics execute(DateRange dateRange, AcademicProgram academicProgram) {
        IntegrationMetrics metrics = calculationService.calculate(dateRange, academicProgram);

        if (metrics == null) {
            throw new NoDataFoundException("No se encontraron datos para los filtros especificados");
        }

        return anonymizationService.ensureNoPii(metrics);
    }
}

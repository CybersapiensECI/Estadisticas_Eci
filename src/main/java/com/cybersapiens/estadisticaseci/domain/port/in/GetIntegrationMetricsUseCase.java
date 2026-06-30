package com.cybersapiens.estadisticaseci.domain.port.in;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.IntegrationMetrics;

public interface GetIntegrationMetricsUseCase {

    IntegrationMetrics execute(DateRange dateRange, AcademicProgram academicProgram);
}

package com.cybersapiens.estadisticaseci.infrastructure.web;

import com.cybersapiens.estadisticaseci.domain.model.IntegrationMetrics;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.infrastructure.web.dto.request.MetricsFilterRequest;
import com.cybersapiens.estadisticaseci.infrastructure.web.dto.response.IntegrationMetricsResponse;
import com.cybersapiens.estadisticaseci.infrastructure.web.mapper.MetricsCsvSerializer;
import com.cybersapiens.estadisticaseci.infrastructure.web.mapper.MetricsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final GetIntegrationMetricsUseCase useCase;
    private final MetricsWebMapper webMapper;
    private final MetricsCsvSerializer csvSerializer;

    public MetricsController(GetIntegrationMetricsUseCase useCase,
                             MetricsWebMapper webMapper,
                             MetricsCsvSerializer csvSerializer) {
        this.useCase = useCase;
        this.webMapper = webMapper;
        this.csvSerializer = csvSerializer;
    }

    @GetMapping("/integration")
    @PreAuthorize("hasAnyRole('ADMIN', 'WELLBEING')")
    public ResponseEntity<?> getIntegrationMetrics(
            @Valid MetricsFilterRequest filter,
            @RequestParam(defaultValue = "json") String format) {

        var dateRange = webMapper.toDateRange(filter);
        var academicProgram = webMapper.toAcademicProgram(filter);

        IntegrationMetrics metrics = useCase.execute(dateRange, academicProgram);

        if ("csv".equalsIgnoreCase(format)) {
            String csv = csvSerializer.toCsv(metrics);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=integration-metrics.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);
        }

        IntegrationMetricsResponse response = webMapper.toResponse(metrics);
        return ResponseEntity.ok(response);
    }
}

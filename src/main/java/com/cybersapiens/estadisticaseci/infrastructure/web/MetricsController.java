package com.cybersapiens.estadisticaseci.infrastructure.web;

import com.cybersapiens.estadisticaseci.domain.model.IntegrationMetrics;
import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.domain.port.in.GetUserPersonalStatsUseCase;
import com.cybersapiens.estadisticaseci.infrastructure.web.client.dto.UserPersonalStatsResponse;
import com.cybersapiens.estadisticaseci.infrastructure.web.dto.request.MetricsFilterRequest;
import com.cybersapiens.estadisticaseci.infrastructure.web.dto.response.IntegrationMetricsResponse;
import com.cybersapiens.estadisticaseci.infrastructure.web.mapper.MetricsCsvSerializer;
import com.cybersapiens.estadisticaseci.infrastructure.web.mapper.MetricsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final GetIntegrationMetricsUseCase useCase;
    private final MetricsWebMapper webMapper;
    private final MetricsCsvSerializer csvSerializer;
    private final GetUserPersonalStatsUseCase userStatsUseCase;

    public MetricsController(GetIntegrationMetricsUseCase useCase,
                             MetricsWebMapper webMapper,
                             MetricsCsvSerializer csvSerializer,
                             GetUserPersonalStatsUseCase userStatsUseCase) {
        this.useCase = useCase;
        this.webMapper = webMapper;
        this.csvSerializer = csvSerializer;
        this.userStatsUseCase = userStatsUseCase;
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

    @GetMapping("/user/{userId}")
    @PreAuthorize("authentication.name == #userId or hasRole('ADMIN') or hasRole('WELLBEING')")
    public ResponseEntity<UserPersonalStatsResponse> getUserPersonalStats(@PathVariable String userId) {
        UserPersonalStats stats = userStatsUseCase.execute(userId);
        return ResponseEntity.ok(UserPersonalStatsResponse.fromDomain(stats));
    }
}

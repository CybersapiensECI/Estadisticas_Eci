package com.cybersapiens.estadisticaseci.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MetricsFilterRequest(
        @NotNull(message = "dateFrom is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @NotNull(message = "dateTo is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        String academicProgram
) {
}

package com.cybersapiens.estadisticaseci.domain.model;

import java.time.LocalDate;

public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("dateFrom must not be after dateTo");
        }
    }
}

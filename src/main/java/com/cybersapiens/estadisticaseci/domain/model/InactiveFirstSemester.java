package com.cybersapiens.estadisticaseci.domain.model;

public record InactiveFirstSemester(double percentage, long inactiveCount, long totalCount) {

    public InactiveFirstSemester {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }
}

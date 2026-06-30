package com.cybersapiens.estadisticaseci.domain.model;

public record NewConnectionRate(double percentage, long connectedCount, long totalCount) {

    public NewConnectionRate {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
    }
}

package com.cybersapiens.estadisticaseci.domain.model;

public record AcademicProgram(String code) {

    public AcademicProgram {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Academic program code must not be blank");
        }
    }
}

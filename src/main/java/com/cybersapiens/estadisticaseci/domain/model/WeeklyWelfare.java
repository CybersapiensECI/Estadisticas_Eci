package com.cybersapiens.estadisticaseci.domain.model;

import java.time.LocalDate;

public record WeeklyWelfare(LocalDate weekStart, long checkinCount, long interventionCount, long uniqueStudents) {
}

package com.cybersapiens.estadisticaseci.domain.service;

import com.cybersapiens.estadisticaseci.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnonymizationServiceTest {

    private AnonymizationService service;

    @BeforeEach
    void setUp() {
        service = new AnonymizationService();
    }

    @Test
    void ensureNoPii_shouldPassCleanMetrics() {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(75.0, 3, 4),
                new InactiveFirstSemester(25.0, 1, 4),
                List.of(new MentorshipByProgram("ING-COMP", "Ingeniería en Computación", 2)),
                List.of(new WeeklyWelfare(LocalDate.of(2026, 6, 1), 2, 1, 2))
        );

        IntegrationMetrics result = service.ensureNoPii(metrics);

        assertThat(result).isSameAs(metrics);
    }

    @Test
    void ensureNoPii_shouldThrowOnEmailInProgramName() {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(75.0, 3, 4),
                new InactiveFirstSemester(25.0, 1, 4),
                List.of(new MentorshipByProgram("ING-COMP", "user@example.com", 2)),
                List.of()
        );

        assertThatThrownBy(() -> service.ensureNoPii(metrics))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("PII detected");
    }

    @Test
    void ensureNoPii_shouldThrowOnUserIdInProgramCode() {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(75.0, 3, 4),
                new InactiveFirstSemester(25.0, 1, 4),
                List.of(new MentorshipByProgram("user-042", "Medicina", 2)),
                List.of()
        );

        assertThatThrownBy(() -> service.ensureNoPii(metrics))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("PII detected");
    }

    @Test
    void ensureNoPii_shouldPassWithEmptyMentorships() {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(0.0, 0, 0),
                new InactiveFirstSemester(0.0, 0, 0),
                List.of(),
                List.of()
        );

        IntegrationMetrics result = service.ensureNoPii(metrics);

        assertThat(result).isSameAs(metrics);
    }
}

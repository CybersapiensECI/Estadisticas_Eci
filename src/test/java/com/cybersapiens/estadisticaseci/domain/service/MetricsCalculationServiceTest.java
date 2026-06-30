package com.cybersapiens.estadisticaseci.domain.service;

import com.cybersapiens.estadisticaseci.domain.model.*;
import com.cybersapiens.estadisticaseci.domain.port.out.ActivityDataPort;
import com.cybersapiens.estadisticaseci.domain.port.out.MentorshipDataPort;
import com.cybersapiens.estadisticaseci.domain.port.out.WelfareDataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsCalculationServiceTest {

    @Mock
    private ActivityDataPort activityDataPort;

    @Mock
    private MentorshipDataPort mentorshipDataPort;

    @Mock
    private WelfareDataPort welfareDataPort;

    private MetricsCalculationService service;

    @BeforeEach
    void setUp() {
        service = new MetricsCalculationService(activityDataPort, mentorshipDataPort, welfareDataPort);
    }

    @Test
    void calculate_shouldAssembleAllMetrics() {
        DateRange dateRange = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        AcademicProgram program = new AcademicProgram("ING-COMP");

        NewConnectionRate connectionRate = new NewConnectionRate(75.0, 3, 4);
        InactiveFirstSemester inactive = new InactiveFirstSemester(25.0, 1, 4);
        List<MentorshipByProgram> mentorships = List.of(
                new MentorshipByProgram("ING-COMP", "Ingeniería en Computación", 2)
        );
        List<WeeklyWelfare> welfare = List.of(
                new WeeklyWelfare(LocalDate.of(2026, 6, 1), 2, 1, 2)
        );

        when(activityDataPort.queryNewConnectionRate(dateRange, program)).thenReturn(connectionRate);
        when(activityDataPort.queryInactiveFirstSemester(dateRange, program)).thenReturn(inactive);
        when(mentorshipDataPort.countByProgram(dateRange, program)).thenReturn(mentorships);
        when(welfareDataPort.getWeeklyIndicators(dateRange, program)).thenReturn(welfare);

        IntegrationMetrics result = service.calculate(dateRange, program);

        assertThat(result.newConnectionRate()).isEqualTo(connectionRate);
        assertThat(result.inactiveFirstSemester()).isEqualTo(inactive);
        assertThat(result.mentorshipsByProgram()).isEqualTo(mentorships);
        assertThat(result.weeklyWelfare()).isEqualTo(welfare);
    }

    @Test
    void calculate_shouldReturnMetricsWithNullProgram() {
        DateRange dateRange = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        NewConnectionRate connectionRate = new NewConnectionRate(50.0, 5, 10);
        InactiveFirstSemester inactive = new InactiveFirstSemester(50.0, 5, 10);

        when(activityDataPort.queryNewConnectionRate(dateRange, null)).thenReturn(connectionRate);
        when(activityDataPort.queryInactiveFirstSemester(dateRange, null)).thenReturn(inactive);
        when(mentorshipDataPort.countByProgram(dateRange, null)).thenReturn(List.of());
        when(welfareDataPort.getWeeklyIndicators(dateRange, null)).thenReturn(List.of());

        IntegrationMetrics result = service.calculate(dateRange, null);

        assertThat(result.newConnectionRate()).isEqualTo(connectionRate);
        assertThat(result.inactiveFirstSemester()).isEqualTo(inactive);
        assertThat(result.mentorshipsByProgram()).isEmpty();
        assertThat(result.weeklyWelfare()).isEmpty();
    }
}

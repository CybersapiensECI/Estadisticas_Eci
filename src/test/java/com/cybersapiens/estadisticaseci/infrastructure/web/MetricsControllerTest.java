package com.cybersapiens.estadisticaseci.infrastructure.web;

import com.cybersapiens.estadisticaseci.domain.model.*;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.infrastructure.web.mapper.MetricsCsvSerializer;
import com.cybersapiens.estadisticaseci.infrastructure.web.mapper.MetricsWebMapper;
import com.cybersapiens.estadisticaseci.shared.exception.NoDataFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetricsController.class)
@Import({MetricsWebMapper.class, MetricsCsvSerializer.class})
@WithMockUser(roles = "ADMIN")
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetIntegrationMetricsUseCase useCase;

    @Test
    void getIntegrationMetrics_shouldReturnJson() throws Exception {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(75.0, 3, 4),
                new InactiveFirstSemester(25.0, 1, 4),
                List.of(new MentorshipByProgram("ING-COMP", "Ingeniería en Computación", 2)),
                List.of(new WeeklyWelfare(LocalDate.of(2026, 6, 1), 2, 1, 2))
        );

        when(useCase.execute(any(), any())).thenReturn(metrics);

        mockMvc.perform(get("/api/v1/metrics/integration")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.newConnectionRate.percentage").value(75.0))
                .andExpect(jsonPath("$.newConnectionRate.connectedCount").value(3))
                .andExpect(jsonPath("$.inactiveFirstSemester.percentage").value(25.0))
                .andExpect(jsonPath("$.mentorshipsByProgram.length()").value(1))
                .andExpect(jsonPath("$.weeklyWelfare.length()").value(1));
    }

    @Test
    void getIntegrationMetrics_shouldReturnCsv() throws Exception {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(75.0, 3, 4),
                new InactiveFirstSemester(25.0, 1, 4),
                List.of(new MentorshipByProgram("ING-COMP", "Ingeniería en Computación", 2)),
                List.of(new WeeklyWelfare(LocalDate.of(2026, 6, 1), 2, 1, 2))
        );

        when(useCase.execute(any(), any())).thenReturn(metrics);

        mockMvc.perform(get("/api/v1/metrics/integration")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-06-30")
                        .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("75.00")));
    }

    @Test
    void getIntegrationMetrics_whenNoData_shouldReturnEmptyResponse() throws Exception {
        when(useCase.execute(any(), any())).thenThrow(new NoDataFoundException("No hay datos"));

        mockMvc.perform(get("/api/v1/metrics/integration")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newConnectionRate.percentage").value(0.0))
                .andExpect(jsonPath("$.newConnectionRate.connectedCount").value(0))
                .andExpect(jsonPath("$.mentorshipsByProgram.length()").value(0))
                .andExpect(jsonPath("$.weeklyWelfare.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "WELLBEING")
    void getIntegrationMetrics_withWellbeingRole_shouldReturnOk() throws Exception {
        IntegrationMetrics metrics = new IntegrationMetrics(
                new NewConnectionRate(50.0, 1, 2),
                new InactiveFirstSemester(50.0, 1, 2),
                List.of(),
                List.of()
        );

        when(useCase.execute(any(), any())).thenReturn(metrics);

        mockMvc.perform(get("/api/v1/metrics/integration")
                        .param("dateFrom", "2026-01-01")
                        .param("dateTo", "2026-06-30"))
                .andExpect(status().isOk());
    }
}

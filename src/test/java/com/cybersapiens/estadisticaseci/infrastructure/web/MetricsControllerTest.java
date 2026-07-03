package com.cybersapiens.estadisticaseci.infrastructure.web;

import com.cybersapiens.estadisticaseci.domain.model.*;
import com.cybersapiens.estadisticaseci.domain.port.in.GetIntegrationMetricsUseCase;
import com.cybersapiens.estadisticaseci.domain.port.in.GetUserPersonalStatsUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
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

    @MockBean
    private GetUserPersonalStatsUseCase userStatsUseCase;

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

    @Test
    @WithMockUser(username = "user-own", roles = "USER")
    void getUserPersonalStats_ownUser_shouldReturnOk() throws Exception {
        var gamification = new UserPersonalStats.GamificationStats(100, 1, 0, 0, 100.0, List.of());
        var events = new UserPersonalStats.EventStats(2, 0, 2, List.of("e1", "e2"));
        var parches = new UserPersonalStats.ParcheStats(1, 1);
        var profile = new UserPersonalStats.ProfileStats(100, 2, true, "ING-COMP", 3);
        var stats = new UserPersonalStats("user-own", gamification, events, parches, profile);

        when(userStatsUseCase.execute(eq("user-own"))).thenReturn(stats);

        mockMvc.perform(get("/api/v1/metrics/user/{userId}", "user-own"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-own"))
                .andExpect(jsonPath("$.gamification.totalXp").value(100))
                .andExpect(jsonPath("$.events.totalAttended").value(2))
                .andExpect(jsonPath("$.parches.totalJoined").value(1))
                .andExpect(jsonPath("$.profile.career").value("ING-COMP"));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    void getUserPersonalStats_withAdminRole_shouldReturnOk() throws Exception {
        var gamification = new UserPersonalStats.GamificationStats(0, 0, 0, 0, 0, List.of());
        var events = new UserPersonalStats.EventStats(0, 0, 0, List.of());
        var parches = new UserPersonalStats.ParcheStats(0, 0);
        var profile = new UserPersonalStats.ProfileStats(0, 1, false, null, null);
        var stats = new UserPersonalStats("any-user", gamification, events, parches, profile);

        when(userStatsUseCase.execute(eq("any-user"))).thenReturn(stats);

        mockMvc.perform(get("/api/v1/metrics/user/{userId}", "any-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("any-user"));
    }

    @Test
    @WithMockUser(username = "user-partial", roles = "USER")
    void getUserPersonalStats_whenServiceFails_shouldReturnPartialData() throws Exception {
        var events = new UserPersonalStats.EventStats(1, 0, 1, List.of("evt-1"));
        var parches = new UserPersonalStats.ParcheStats(0, 0);
        var profile = new UserPersonalStats.ProfileStats(0, 1, false, null, null);
        var stats = new UserPersonalStats("user-partial", null, events, parches, profile);

        when(userStatsUseCase.execute(eq("user-partial"))).thenReturn(stats);

        mockMvc.perform(get("/api/v1/metrics/user/{userId}", "user-partial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gamification").doesNotExist())
                .andExpect(jsonPath("$.events.totalAttended").value(1))
                .andExpect(jsonPath("$.parches.totalJoined").value(0));
    }
}

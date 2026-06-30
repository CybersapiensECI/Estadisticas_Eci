package com.cybersapiens.estadisticaseci.infrastructure.web.mapper;

import com.cybersapiens.estadisticaseci.domain.model.*;
import com.cybersapiens.estadisticaseci.infrastructure.web.dto.request.MetricsFilterRequest;
import com.cybersapiens.estadisticaseci.infrastructure.web.dto.response.IntegrationMetricsResponse;
import org.springframework.stereotype.Component;

@Component
public class MetricsWebMapper {

    public DateRange toDateRange(MetricsFilterRequest request) {
        return new DateRange(request.dateFrom(), request.dateTo());
    }

    public AcademicProgram toAcademicProgram(MetricsFilterRequest request) {
        if (request.academicProgram() == null || request.academicProgram().isBlank()) {
            return null;
        }
        return new AcademicProgram(request.academicProgram());
    }

    public IntegrationMetricsResponse toResponse(IntegrationMetrics domain) {
        return new IntegrationMetricsResponse(
                new IntegrationMetricsResponse.NewConnectionRateResponse(
                        domain.newConnectionRate().percentage(),
                        domain.newConnectionRate().connectedCount(),
                        domain.newConnectionRate().totalCount()
                ),
                new IntegrationMetricsResponse.InactiveFirstSemesterResponse(
                        domain.inactiveFirstSemester().percentage(),
                        domain.inactiveFirstSemester().inactiveCount(),
                        domain.inactiveFirstSemester().totalCount()
                ),
                domain.mentorshipsByProgram().stream()
                        .map(m -> new IntegrationMetricsResponse.MentorshipByProgramResponse(
                                m.programCode(), m.programName(), m.mentorshipCount()))
                        .toList(),
                domain.weeklyWelfare().stream()
                        .map(w -> new IntegrationMetricsResponse.WeeklyWelfareResponse(
                                w.weekStart().toString(),
                                w.checkinCount(),
                                w.interventionCount(),
                                w.uniqueStudents()))
                        .toList()
        );
    }
}

package com.cybersapiens.estadisticaseci.infrastructure.web.dto.response;

import java.util.List;

public record IntegrationMetricsResponse(
        NewConnectionRateResponse newConnectionRate,
        InactiveFirstSemesterResponse inactiveFirstSemester,
        List<MentorshipByProgramResponse> mentorshipsByProgram,
        List<WeeklyWelfareResponse> weeklyWelfare
) {

    public record NewConnectionRateResponse(double percentage, long connectedCount, long totalCount) {
    }

    public record InactiveFirstSemesterResponse(double percentage, long inactiveCount, long totalCount) {
    }

    public record MentorshipByProgramResponse(String programCode, String programName, long mentorshipCount) {
    }

    public record WeeklyWelfareResponse(String weekStart, long checkinCount, long interventionCount, long uniqueStudents) {
    }
}

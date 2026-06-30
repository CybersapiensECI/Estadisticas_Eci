package com.cybersapiens.estadisticaseci.domain.model;

import java.util.List;

public record IntegrationMetrics(
        NewConnectionRate newConnectionRate,
        InactiveFirstSemester inactiveFirstSemester,
        List<MentorshipByProgram> mentorshipsByProgram,
        List<WeeklyWelfare> weeklyWelfare
) {
}

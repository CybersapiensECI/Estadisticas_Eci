package com.cybersapiens.estadisticaseci.domain.service;

import com.cybersapiens.estadisticaseci.domain.model.*;
import com.cybersapiens.estadisticaseci.domain.port.out.ActivityDataPort;
import com.cybersapiens.estadisticaseci.domain.port.out.MentorshipDataPort;
import com.cybersapiens.estadisticaseci.domain.port.out.WelfareDataPort;

import java.util.List;

public class MetricsCalculationService {

    private final ActivityDataPort activityDataPort;
    private final MentorshipDataPort mentorshipDataPort;
    private final WelfareDataPort welfareDataPort;

    public MetricsCalculationService(ActivityDataPort activityDataPort,
                                     MentorshipDataPort mentorshipDataPort,
                                     WelfareDataPort welfareDataPort) {
        this.activityDataPort = activityDataPort;
        this.mentorshipDataPort = mentorshipDataPort;
        this.welfareDataPort = welfareDataPort;
    }

    public IntegrationMetrics calculate(DateRange dateRange, AcademicProgram academicProgram) {
        NewConnectionRate connectionRate = activityDataPort.queryNewConnectionRate(dateRange, academicProgram);
        InactiveFirstSemester inactive = activityDataPort.queryInactiveFirstSemester(dateRange, academicProgram);
        List<MentorshipByProgram> mentorships = mentorshipDataPort.countByProgram(dateRange, academicProgram);
        List<WeeklyWelfare> welfare = welfareDataPort.getWeeklyIndicators(dateRange, academicProgram);

        return new IntegrationMetrics(connectionRate, inactive, mentorships, welfare);
    }
}

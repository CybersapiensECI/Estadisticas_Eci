package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.WeeklyWelfare;

import java.util.List;

public interface WelfareDataPort {

    List<WeeklyWelfare> getWeeklyIndicators(DateRange dateRange, AcademicProgram academicProgram);
}

package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.InactiveFirstSemester;
import com.cybersapiens.estadisticaseci.domain.model.NewConnectionRate;

public interface ActivityDataPort {

    NewConnectionRate queryNewConnectionRate(DateRange dateRange, AcademicProgram academicProgram);

    InactiveFirstSemester queryInactiveFirstSemester(DateRange dateRange, AcademicProgram academicProgram);
}

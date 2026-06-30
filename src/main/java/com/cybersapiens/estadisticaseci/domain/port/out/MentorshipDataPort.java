package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.MentorshipByProgram;

import java.util.List;

public interface MentorshipDataPort {

    List<MentorshipByProgram> countByProgram(DateRange dateRange, AcademicProgram academicProgram);
}

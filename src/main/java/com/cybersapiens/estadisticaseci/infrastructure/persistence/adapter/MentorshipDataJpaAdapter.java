package com.cybersapiens.estadisticaseci.infrastructure.persistence.adapter;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.MentorshipByProgram;
import com.cybersapiens.estadisticaseci.domain.port.out.MentorshipDataPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MentorshipDataJpaAdapter implements MentorshipDataPort {

    private final MentorshipJpaRepository repository;

    public MentorshipDataJpaAdapter(MentorshipJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MentorshipByProgram> countByProgram(DateRange dateRange, AcademicProgram academicProgram) {
        String program = academicProgram != null ? academicProgram.code() : null;

        return repository.countByProgramGrouped(dateRange.from(), dateRange.to(), program);
    }
}

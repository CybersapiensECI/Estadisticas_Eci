package com.cybersapiens.estadisticaseci.infrastructure.persistence.adapter;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.InactiveFirstSemester;
import com.cybersapiens.estadisticaseci.domain.model.NewConnectionRate;
import com.cybersapiens.estadisticaseci.domain.port.out.ActivityDataPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class ActivityDataJpaAdapter implements ActivityDataPort {

    private final ActivityRecordJpaRepository repository;

    public ActivityDataJpaAdapter(ActivityRecordJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public NewConnectionRate queryNewConnectionRate(DateRange dateRange, AcademicProgram academicProgram) {
        String program = academicProgram != null ? academicProgram.code() : null;
        LocalDateTime from = dateRange.from().atStartOfDay();
        LocalDateTime to = dateRange.to().atTime(LocalTime.MAX);

        long connected = repository.countDistinctUsersWithActivity(from, to, program);
        long total = repository.countDistinctUsers(from, to, program);

        double percentage = total > 0 ? (double) connected / total * 100 : 0.0;
        return new NewConnectionRate(percentage, connected, total);
    }

    @Override
    public InactiveFirstSemester queryInactiveFirstSemester(DateRange dateRange, AcademicProgram academicProgram) {
        String program = academicProgram != null ? academicProgram.code() : null;
        LocalDateTime from = dateRange.from().atStartOfDay();
        LocalDateTime to = dateRange.to().atTime(LocalTime.MAX);

        long total = repository.countDistinctUsers(from, to, program);
        long connected = repository.countDistinctUsersWithActivity(from, to, program);
        long inactive = total - connected;

        double percentage = total > 0 ? (double) inactive / total * 100 : 0.0;
        return new InactiveFirstSemester(percentage, inactive, total);
    }
}

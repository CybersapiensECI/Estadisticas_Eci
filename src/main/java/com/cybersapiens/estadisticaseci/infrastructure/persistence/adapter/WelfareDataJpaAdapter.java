package com.cybersapiens.estadisticaseci.infrastructure.persistence.adapter;

import com.cybersapiens.estadisticaseci.domain.model.AcademicProgram;
import com.cybersapiens.estadisticaseci.domain.model.DateRange;
import com.cybersapiens.estadisticaseci.domain.model.WeeklyWelfare;
import com.cybersapiens.estadisticaseci.domain.port.out.WelfareDataPort;
import com.cybersapiens.estadisticaseci.infrastructure.persistence.entity.WelfareCheckinEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Component
public class WelfareDataJpaAdapter implements WelfareDataPort {

    private final WelfareCheckinJpaRepository repository;

    public WelfareDataJpaAdapter(WelfareCheckinJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<WeeklyWelfare> getWeeklyIndicators(DateRange dateRange, AcademicProgram academicProgram) {
        String program = academicProgram != null ? academicProgram.code() : null;

        List<WelfareCheckinEntity> records = repository.findByDateRange(
                dateRange.from(), dateRange.to(), program);

        Map<LocalDate, WeeklyAccumulator> weeklyMap = new TreeMap<>();

        for (WelfareCheckinEntity record : records) {
            LocalDate weekStart = getWeekStart(record.getCheckinDate());
            weeklyMap.computeIfAbsent(weekStart, k -> new WeeklyAccumulator())
                    .add(record);
        }

        return weeklyMap.entrySet().stream()
                .map(e -> new WeeklyWelfare(
                        e.getKey(),
                        e.getValue().checkinCount,
                        e.getValue().interventionCount,
                        e.getValue().getUniqueStudents()))
                .toList();
    }

    private LocalDate getWeekStart(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    private static class WeeklyAccumulator {
        long checkinCount = 0;
        long interventionCount = 0;
        final Set<String> students = new HashSet<>();

        void add(WelfareCheckinEntity record) {
            checkinCount++;
            if (record.getInterventionType() != null && !record.getInterventionType().isBlank()) {
                interventionCount++;
            }
            students.add(record.getUserId());
        }

        long getUniqueStudents() {
            return students.size();
        }
    }
}

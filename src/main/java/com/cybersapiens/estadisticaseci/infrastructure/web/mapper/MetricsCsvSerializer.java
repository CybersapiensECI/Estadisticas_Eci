package com.cybersapiens.estadisticaseci.infrastructure.web.mapper;

import com.cybersapiens.estadisticaseci.domain.model.IntegrationMetrics;
import com.cybersapiens.estadisticaseci.domain.model.MentorshipByProgram;
import com.cybersapiens.estadisticaseci.domain.model.WeeklyWelfare;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.List;

@Component
public class MetricsCsvSerializer {

    public String toCsv(IntegrationMetrics metrics) {
        StringWriter stringWriter = new StringWriter();
        try (CSVWriter writer = new CSVWriter(stringWriter)) {

            writer.writeNext(new String[]{"--- New Connection Rate ---"});
            writer.writeNext(new String[]{"Percentage", "Connected Count", "Total Count"});
            writer.writeNext(new String[]{
                    String.format("%.2f", metrics.newConnectionRate().percentage()),
                    String.valueOf(metrics.newConnectionRate().connectedCount()),
                    String.valueOf(metrics.newConnectionRate().totalCount())
            });

            writer.writeNext(new String[]{});
            writer.writeNext(new String[]{"--- Inactive First Semester ---"});
            writer.writeNext(new String[]{"Percentage", "Inactive Count", "Total Count"});
            writer.writeNext(new String[]{
                    String.format("%.2f", metrics.inactiveFirstSemester().percentage()),
                    String.valueOf(metrics.inactiveFirstSemester().inactiveCount()),
                    String.valueOf(metrics.inactiveFirstSemester().totalCount())
            });

            writer.writeNext(new String[]{});
            writer.writeNext(new String[]{"--- Mentorships by Program ---"});
            writer.writeNext(new String[]{"Program Code", "Program Name", "Mentorship Count"});
            List<MentorshipByProgram> mentorships = metrics.mentorshipsByProgram();
            if (mentorships.isEmpty()) {
                writer.writeNext(new String[]{"No data", "", ""});
            } else {
                for (MentorshipByProgram m : mentorships) {
                    writer.writeNext(new String[]{
                            m.programCode(), m.programName(), String.valueOf(m.mentorshipCount())
                    });
                }
            }

            writer.writeNext(new String[]{});
            writer.writeNext(new String[]{"--- Weekly Welfare ---"});
            writer.writeNext(new String[]{"Week Start", "Checkin Count", "Intervention Count", "Unique Students"});
            List<WeeklyWelfare> welfare = metrics.weeklyWelfare();
            if (welfare.isEmpty()) {
                writer.writeNext(new String[]{"No data", "", "", ""});
            } else {
                for (WeeklyWelfare w : welfare) {
                    writer.writeNext(new String[]{
                            w.weekStart().toString(),
                            String.valueOf(w.checkinCount()),
                            String.valueOf(w.interventionCount()),
                            String.valueOf(w.uniqueStudents())
                    });
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV", e);
        }

        return stringWriter.toString();
    }
}

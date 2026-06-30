package com.cybersapiens.estadisticaseci.domain.service;

import com.cybersapiens.estadisticaseci.domain.model.IntegrationMetrics;
import com.cybersapiens.estadisticaseci.domain.model.MentorshipByProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AnonymizationService {

    private static final Logger log = LoggerFactory.getLogger(AnonymizationService.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern USER_ID_PATTERN =
            Pattern.compile("user-\\d+", Pattern.CASE_INSENSITIVE);

    public IntegrationMetrics ensureNoPii(IntegrationMetrics metrics) {
        List<String> stringFields = collectStringFields(metrics);

        for (String field : stringFields) {
            if (field == null) continue;

            if (EMAIL_PATTERN.matcher(field).find()) {
                log.warn("PII detection triggered: email pattern found in metrics");
                throw new SecurityException("PII detected: email found in aggregated metrics");
            }
            if (USER_ID_PATTERN.matcher(field).find()) {
                log.warn("PII detection triggered: user ID pattern found in metrics");
                throw new SecurityException("PII detected: user identifier found in aggregated metrics");
            }
        }

        log.debug("PII check passed: no personal data detected in metrics");
        return metrics;
    }

    private List<String> collectStringFields(IntegrationMetrics metrics) {
        List<String> fields = new ArrayList<>();

        if (metrics.mentorshipsByProgram() != null) {
            for (MentorshipByProgram m : metrics.mentorshipsByProgram()) {
                fields.add(m.programCode());
                fields.add(m.programName());
            }
        }

        return fields;
    }
}

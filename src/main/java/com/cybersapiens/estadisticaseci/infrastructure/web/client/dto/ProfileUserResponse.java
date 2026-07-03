package com.cybersapiens.estadisticaseci.infrastructure.web.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileUserResponse(
        String id,
        String name,
        String userType,
        String career,
        Integer semester,
        Integer xp,
        Integer level,
        Boolean active
) {}

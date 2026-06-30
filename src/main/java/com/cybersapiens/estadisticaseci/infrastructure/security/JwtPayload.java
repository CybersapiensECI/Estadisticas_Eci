package com.cybersapiens.estadisticaseci.infrastructure.security;

import java.util.List;

public record JwtPayload(String subject, List<String> roles) {
}

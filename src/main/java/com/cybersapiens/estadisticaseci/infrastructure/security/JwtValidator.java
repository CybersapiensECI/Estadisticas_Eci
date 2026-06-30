package com.cybersapiens.estadisticaseci.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public final class JwtValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtValidator() {
    }

    @SuppressWarnings("unchecked")
    public static JwtPayload validate(String token, String secret) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String signature = computeHmac(parts[0] + "." + parts[1], secret);
            if (!signature.equals(parts[2])) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = MAPPER.readValue(payloadJson, Map.class);

            String subject = (String) claims.get("sub");
            List<String> roles;
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?> roleList) {
                roles = roleList.stream().map(Object::toString).toList();
            } else {
                roles = List.of();
            }

            return new JwtPayload(subject, roles);
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT validation failed", e);
        }
    }

    private static String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}

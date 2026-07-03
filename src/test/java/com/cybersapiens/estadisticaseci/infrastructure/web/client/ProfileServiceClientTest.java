package com.cybersapiens.estadisticaseci.infrastructure.web.client;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServiceClientTest {

    private MockWebServer mockWebServer;
    private ProfileServiceClient client;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        client = new ProfileServiceClient(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getProfileStats_shouldMapStudentResponse() throws Exception {
        String json = """
                {
                    "id": "user-123",
                    "name": "Juan Perez",
                    "userType": "STUDENT",
                    "career": "SYSTEMS_ENGINEERING",
                    "semester": 5,
                    "xp": 1200,
                    "level": 4,
                    "active": true
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.ProfileStats result = client.getProfileStats("user-123");

        assertEquals(1200, result.xp());
        assertEquals(4, result.level());
        assertTrue(result.isActive());
        assertEquals("SYSTEMS_ENGINEERING", result.career());
        assertEquals(5, result.semester());

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("/api/v1/users/user-123"));
    }

    @Test
    void getProfileStats_whenNullableFieldsAreNull_shouldUseDefaults() throws Exception {
        String json = """
                {
                    "id": "user-456",
                    "name": "Maria Lopez",
                    "userType": "STUDENT",
                    "career": null,
                    "semester": null,
                    "xp": null,
                    "level": null,
                    "active": null
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.ProfileStats result = client.getProfileStats("user-456");

        assertEquals(0, result.xp());
        assertEquals(1, result.level());
        assertFalse(result.isActive());
        assertNull(result.career());
        assertNull(result.semester());
    }

    @Test
    void getProfileStats_whenServerError_shouldThrowException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(Exception.class, () -> client.getProfileStats("user-123"));
    }
}

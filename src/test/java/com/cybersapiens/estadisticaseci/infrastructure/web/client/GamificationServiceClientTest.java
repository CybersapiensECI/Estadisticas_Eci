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

class GamificationServiceClientTest {

    private MockWebServer mockWebServer;
    private GamificationServiceClient client;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        client = new GamificationServiceClient(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getGamificationStats_shouldMapResponseCorrectly() throws Exception {
        String json = """
                {
                    "userId": "user-123",
                    "totalXp": 1500,
                    "totalUnlocked": 2,
                    "unlocked": [
                        {"code": "PRIMER_CONTACTO", "name": "Primer Contacto", "rarity": "COMUN", "imageUrl": "/a.png", "xpGranted": 50, "unlockedAt": "2026-07-03T10:30:00"}
                    ],
                    "inProgress": [
                        {"code": "NETWORKING_5", "name": "Networking 5", "rarity": "POCO_COMUN", "imageUrl": "/b.png", "currentCount": 2, "requiredCount": 5, "progressPercentage": 40}
                    ],
                    "locked": [
                        {"code": "NETWORKING_10", "name": "Networking 10", "rarity": "RARO", "imageUrl": "/c.png", "xpGranted": 350, "unlockedAt": null}
                    ]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.GamificationStats result = client.getGamificationStats("user-123");

        assertEquals(1500, result.totalXp());
        assertEquals(2, result.totalMonasUnlocked());
        assertEquals(1, result.monasInProgress());
        assertEquals(1, result.monasLocked());
        assertTrue(result.completionPercentage() > 0);

        assertEquals(3, result.monas().size());
        assertEquals("PRIMER_CONTACTO", result.monas().get(0).code());
        assertEquals("UNLOCKED", result.monas().get(0).status());
        assertEquals("NETWORKING_5", result.monas().get(1).code());
        assertEquals("IN_PROGRESS", result.monas().get(1).status());
        assertEquals("NETWORKING_10", result.monas().get(2).code());
        assertEquals("LOCKED", result.monas().get(2).status());

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("/api/v1/gamification/users/user-123/monas"));
    }

    @Test
    void getGamificationStats_whenEmptyResponse_shouldReturnZeroedStats() throws Exception {
        String json = """
                {
                    "userId": "user-123",
                    "totalXp": 0,
                    "totalUnlocked": 0,
                    "unlocked": [],
                    "inProgress": [],
                    "locked": []
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.GamificationStats result = client.getGamificationStats("user-123");

        assertEquals(0, result.totalXp());
        assertEquals(0, result.totalMonasUnlocked());
        assertEquals(0, result.monasInProgress());
        assertEquals(0, result.monasLocked());
        assertEquals(0, result.completionPercentage());
        assertTrue(result.monas().isEmpty());
    }

    @Test
    void getGamificationStats_whenServerError_shouldThrowException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThrows(Exception.class, () -> client.getGamificationStats("user-123"));
    }
}

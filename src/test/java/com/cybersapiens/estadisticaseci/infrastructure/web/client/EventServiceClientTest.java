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

class EventServiceClientTest {

    private MockWebServer mockWebServer;
    private EventServiceClient client;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        client = new EventServiceClient(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getEventStats_shouldMapResponseCorrectly() throws Exception {
        String json = """
                ["evt-001", "evt-002", "evt-003"]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.EventStats result = client.getEventStats("user-123");

        assertEquals(3, result.totalAttended());
        assertEquals(3, result.totalEvents());
        assertEquals(3, result.eventIds().size());
        assertTrue(result.eventIds().contains("evt-002"));

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("/events/agenda"));
        assertTrue(request.getPath().contains("userId=user-123"));
    }

    @Test
    void getEventStats_whenEmptyAgenda_shouldReturnZeroedStats() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.EventStats result = client.getEventStats("user-123");

        assertEquals(0, result.totalAttended());
        assertEquals(0, result.totalEvents());
        assertTrue(result.eventIds().isEmpty());
    }

    @Test
    void getEventStats_whenServerError_shouldThrowException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(Exception.class, () -> client.getEventStats("user-123"));
    }
}

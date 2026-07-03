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

class ParcheServiceClientTest {

    private MockWebServer mockWebServer;
    private ParcheServiceClient client;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        client = new ParcheServiceClient(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getParcheStats_shouldReturnJoinedAndActiveCounts() throws Exception {
        String json = """
                [
                    {"id": "p1", "name": "Parche Deportivo", "status": "ACTIVE"},
                    {"id": "p2", "name": "Parche Gaming", "status": "ACTIVE"},
                    {"id": "p3", "name": "Parche Viejo", "status": "FILED"}
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.ParcheStats result = client.getParcheStats("user-123");

        assertEquals(3, result.totalJoined());
        assertEquals(2, result.activeParches());

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("/api/parches/user/user-123"));
    }

    @Test
    void getParcheStats_whenNoParches_shouldReturnZeroedStats() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.ParcheStats result = client.getParcheStats("user-123");

        assertEquals(0, result.totalJoined());
        assertEquals(0, result.activeParches());
    }

    @Test
    void getParcheStats_whenAllActive_shouldCountCorrectly() throws Exception {
        String json = """
                [
                    {"id": "p1", "name": "A", "status": "ACTIVE"},
                    {"id": "p2", "name": "B", "status": "ACTIVE"}
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json"));

        UserPersonalStats.ParcheStats result = client.getParcheStats("user-123");

        assertEquals(2, result.totalJoined());
        assertEquals(2, result.activeParches());
    }

    @Test
    void getParcheStats_whenServerError_shouldThrowException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(Exception.class, () -> client.getParcheStats("user-123"));
    }
}

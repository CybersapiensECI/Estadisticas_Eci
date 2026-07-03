package com.cybersapiens.estadisticaseci.infrastructure.web.client.dto;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPersonalStatsResponseTest {

    @Test
    void fromDomain_shouldMapAllFields() {
        var monas = List.of(
                new UserPersonalStats.MonaEntry("PRIMER_CONTACTO", "Primer Contacto", "COMUN", "UNLOCKED", "2026-07-03T10:30:00"),
                new UserPersonalStats.MonaEntry("NETWORKING_5", "Networking 5", "POCO_COMUN", "IN_PROGRESS", null)
        );
        var gamification = new UserPersonalStats.GamificationStats(1500, 1, 1, 0, 50.0, monas);
        var events = new UserPersonalStats.EventStats(3, 1, 4, List.of("evt-1", "evt-2"));
        var parches = new UserPersonalStats.ParcheStats(2, 2);
        var profile = new UserPersonalStats.ProfileStats(1500, 5, true, "SYSTEMS_ENGINEERING", 5);

        var domain = new UserPersonalStats("user-123", gamification, events, parches, profile);

        UserPersonalStatsResponse response = UserPersonalStatsResponse.fromDomain(domain);

        assertEquals("user-123", response.userId());
        assertNotNull(response.gamification());
        assertEquals(1500, response.gamification().totalXp());
        assertEquals(1, response.gamification().totalMonasUnlocked());
        assertEquals(2, response.gamification().monas().size());
        assertEquals("PRIMER_CONTACTO", response.gamification().monas().get(0).code());
        assertEquals("UNLOCKED", response.gamification().monas().get(0).status());
        assertEquals("NETWORKING_5", response.gamification().monas().get(1).code());
        assertEquals("IN_PROGRESS", response.gamification().monas().get(1).status());

        assertNotNull(response.events());
        assertEquals(3, response.events().totalAttended());
        assertEquals(2, response.events().eventIds().size());

        assertNotNull(response.parches());
        assertEquals(2, response.parches().totalJoined());

        assertNotNull(response.profile());
        assertEquals(1500, response.profile().xp());
        assertEquals("SYSTEMS_ENGINEERING", response.profile().career());
    }

    @Test
    void fromDomain_whenNullSections_shouldMapToNull() {
        var domain = new UserPersonalStats("user-123", null, null, null, null);

        UserPersonalStatsResponse response = UserPersonalStatsResponse.fromDomain(domain);

        assertEquals("user-123", response.userId());
        assertNull(response.gamification());
        assertNull(response.events());
        assertNull(response.parches());
        assertNull(response.profile());
    }
}

package com.cybersapiens.estadisticaseci.application.handler;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalGamificationPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalEventPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalParchePort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalProfilePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserPersonalStatsHandlerTest {

    @Mock
    private ExternalGamificationPort gamificationPort;

    @Mock
    private ExternalEventPort eventPort;

    @Mock
    private ExternalParchePort parchePort;

    @Mock
    private ExternalProfilePort profilePort;

    private GetUserPersonalStatsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetUserPersonalStatsHandler(gamificationPort, eventPort, parchePort, profilePort);
    }

    @Test
    void execute_allServicesAvailable_shouldReturnAggregatedStats() {
        String userId = "user-123";

        var gamificationStats = new UserPersonalStats.GamificationStats(
                1500, 5, 3, 22, 16.67,
                List.of(new UserPersonalStats.MonaEntry("PRIMER_CONTACTO", "Primer Contacto", "COMUN", "UNLOCKED", "2026-07-03T10:30:00")));
        var eventStats = new UserPersonalStats.EventStats(3, 1, 4, List.of("evt-1", "evt-2", "evt-3", "evt-4"));
        var parcheStats = new UserPersonalStats.ParcheStats(2, 2);
        var profileStats = new UserPersonalStats.ProfileStats(1500, 5, true, "SYSTEMS_ENGINEERING", 5);

        when(gamificationPort.getGamificationStats(userId)).thenReturn(gamificationStats);
        when(eventPort.getEventStats(userId)).thenReturn(eventStats);
        when(parchePort.getParcheStats(userId)).thenReturn(parcheStats);
        when(profilePort.getProfileStats(userId)).thenReturn(profileStats);

        UserPersonalStats result = handler.execute(userId);

        assertEquals(userId, result.userId());
        assertSame(gamificationStats, result.gamification());
        assertSame(eventStats, result.events());
        assertSame(parcheStats, result.parches());
        assertSame(profileStats, result.profile());

        verify(gamificationPort).getGamificationStats(userId);
        verify(eventPort).getEventStats(userId);
        verify(parchePort).getParcheStats(userId);
        verify(profilePort).getProfileStats(userId);
    }

    @Test
    void execute_gamificationFails_shouldReturnPartialStats() {
        String userId = "user-123";

        var eventStats = new UserPersonalStats.EventStats(2, 0, 2, List.of("evt-1", "evt-2"));
        var parcheStats = new UserPersonalStats.ParcheStats(1, 1);
        var profileStats = new UserPersonalStats.ProfileStats(500, 3, true, "CIVIL_ENGINEERING", 3);

        when(gamificationPort.getGamificationStats(userId)).thenThrow(new RuntimeException("Gamification service down"));
        when(eventPort.getEventStats(userId)).thenReturn(eventStats);
        when(parchePort.getParcheStats(userId)).thenReturn(parcheStats);
        when(profilePort.getProfileStats(userId)).thenReturn(profileStats);

        UserPersonalStats result = handler.execute(userId);

        assertEquals(userId, result.userId());
        assertNull(result.gamification());
        assertEquals(2, result.events().totalAttended());
        assertEquals(1, result.parches().totalJoined());
        assertEquals(500, result.profile().xp());
    }

    @Test
    void execute_allServicesFail_shouldReturnAllNull() {
        String userId = "user-123";

        when(gamificationPort.getGamificationStats(userId)).thenThrow(new RuntimeException("Down"));
        when(eventPort.getEventStats(userId)).thenThrow(new RuntimeException("Down"));
        when(parchePort.getParcheStats(userId)).thenThrow(new RuntimeException("Down"));
        when(profilePort.getProfileStats(userId)).thenThrow(new RuntimeException("Down"));

        UserPersonalStats result = handler.execute(userId);

        assertEquals(userId, result.userId());
        assertNull(result.gamification());
        assertNull(result.events());
        assertNull(result.parches());
        assertNull(result.profile());
    }

    @Test
    void execute_shouldCallAllServices() {
        String userId = "user-456";

        when(gamificationPort.getGamificationStats(anyString())).thenReturn(
                new UserPersonalStats.GamificationStats(0, 0, 0, 0, 0, List.of()));
        when(eventPort.getEventStats(anyString())).thenReturn(
                new UserPersonalStats.EventStats(0, 0, 0, List.of()));
        when(parchePort.getParcheStats(anyString())).thenReturn(
                new UserPersonalStats.ParcheStats(0, 0));
        when(profilePort.getProfileStats(anyString())).thenReturn(
                new UserPersonalStats.ProfileStats(0, 1, false, null, null));

        handler.execute(userId);

        verify(gamificationPort).getGamificationStats(userId);
        verify(eventPort).getEventStats(userId);
        verify(parchePort).getParcheStats(userId);
        verify(profilePort).getProfileStats(userId);
    }
}

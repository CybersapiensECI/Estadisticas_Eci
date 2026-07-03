package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;

public interface ExternalGamificationPort {
    UserPersonalStats.GamificationStats getGamificationStats(String userId);
}

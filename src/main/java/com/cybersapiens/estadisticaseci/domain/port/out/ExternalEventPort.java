package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;

public interface ExternalEventPort {
    UserPersonalStats.EventStats getEventStats(String userId);
}

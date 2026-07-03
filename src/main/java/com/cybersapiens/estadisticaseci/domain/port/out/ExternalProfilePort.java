package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;

public interface ExternalProfilePort {
    UserPersonalStats.ProfileStats getProfileStats(String userId);
}

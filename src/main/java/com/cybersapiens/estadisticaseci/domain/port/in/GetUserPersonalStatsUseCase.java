package com.cybersapiens.estadisticaseci.domain.port.in;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;

public interface GetUserPersonalStatsUseCase {
    UserPersonalStats execute(String userId);
}

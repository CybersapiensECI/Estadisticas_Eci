package com.cybersapiens.estadisticaseci.domain.port.out;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;

public interface ExternalParchePort {
    UserPersonalStats.ParcheStats getParcheStats(String userId);
}

package com.cybersapiens.estadisticaseci.application.handler;

import com.cybersapiens.estadisticaseci.domain.model.UserPersonalStats;
import com.cybersapiens.estadisticaseci.domain.port.in.GetUserPersonalStatsUseCase;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalGamificationPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalEventPort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalParchePort;
import com.cybersapiens.estadisticaseci.domain.port.out.ExternalProfilePort;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetUserPersonalStatsHandler implements GetUserPersonalStatsUseCase {

    private final ExternalGamificationPort gamificationPort;
    private final ExternalEventPort eventPort;
    private final ExternalParchePort parchePort;
    private final ExternalProfilePort profilePort;

    public GetUserPersonalStatsHandler(ExternalGamificationPort gamificationPort,
                                       ExternalEventPort eventPort,
                                       ExternalParchePort parchePort,
                                       ExternalProfilePort profilePort) {
        this.gamificationPort = gamificationPort;
        this.eventPort = eventPort;
        this.parchePort = parchePort;
        this.profilePort = profilePort;
    }

    @Override
    public UserPersonalStats execute(String userId) {
        CompletableFuture<UserPersonalStats.GamificationStats> gamificationFuture =
                CompletableFuture.supplyAsync(() -> gamificationPort.getGamificationStats(userId))
                        .exceptionally(ex -> null);

        CompletableFuture<UserPersonalStats.EventStats> eventFuture =
                CompletableFuture.supplyAsync(() -> eventPort.getEventStats(userId))
                        .exceptionally(ex -> null);

        CompletableFuture<UserPersonalStats.ParcheStats> parcheFuture =
                CompletableFuture.supplyAsync(() -> parchePort.getParcheStats(userId))
                        .exceptionally(ex -> null);

        CompletableFuture<UserPersonalStats.ProfileStats> profileFuture =
                CompletableFuture.supplyAsync(() -> profilePort.getProfileStats(userId))
                        .exceptionally(ex -> null);

        CompletableFuture.allOf(gamificationFuture, eventFuture, parcheFuture, profileFuture).join();

        return new UserPersonalStats(
                userId,
                gamificationFuture.join(),
                eventFuture.join(),
                parcheFuture.join(),
                profileFuture.join()
        );
    }
}

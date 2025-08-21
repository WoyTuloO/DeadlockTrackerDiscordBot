package com.woytuloo.discordbot.deadlockservice.utls;

import com.woytuloo.discordbot.entities.deadlockapiresponse.HealthCheckResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class DeadlockUtilsService {

    private static DeadlockUtilsService utils;
    private DeadlockUtilsService() {}

    public static DeadlockUtilsService getInstance() {
        if (utils == null) {
            utils = new DeadlockUtilsService();
        }
        return utils;
    }

    public WebClient webClient = WebClient.builder()
            .baseUrl("https://api.deadlock-api.com/v1")
            .build();


    public HealthCheckResponse runHealthCheck(){
        return webClient.get()
                .uri("/info/health")
                .retrieve()
                .bodyToMono(HealthCheckResponse.class)
                .block();
    }



}

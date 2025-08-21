package com.woytuloo.discordbot.entities.deadlockapiresponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HealthCheckResponse(
        Services services
) {
    public record Services(
            @JsonProperty("clickhouse") boolean clickhouseStatus,
            @JsonProperty("postgres") boolean postgresStatus,
            @JsonProperty("redis") boolean redisStatus,
            @JsonProperty("firebase") boolean firebaseStatus
    ) {}
}

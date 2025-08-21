package com.woytuloo.discordbot.entities.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatchStats(
        @JsonProperty("hero_id") int heroId,
        @JsonProperty("match_result") int matchResult,

        @JsonProperty("net_worth") int netWorth,
        @JsonProperty("denies") int playerDenies,
        @JsonProperty("player_assists") int playerAssists,
        @JsonProperty("player_deaths") int playerDeaths,
        @JsonProperty("player_kills") int playerKills
) {}

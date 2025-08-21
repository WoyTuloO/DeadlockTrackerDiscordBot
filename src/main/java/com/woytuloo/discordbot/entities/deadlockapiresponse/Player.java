package com.woytuloo.discordbot.entities.deadlockapiresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Player {
    @JsonProperty("account_id") String accountId;
    @JsonProperty("hero_id") String heroId;
    @JsonProperty("team") String team;
    @JsonProperty("abandoned") String abandoned;

    String rank;
    String kd;
    String heroKd;
    String avgDeniesPerMatch;
    String accuracy;
    String kills;
    String deaths;
    String assists;
    String wins;
    String matchesPlayedOnHero;
}

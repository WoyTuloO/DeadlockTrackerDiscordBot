package com.woytuloo.discordbot.entities.deadlockapiresponse;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.List;

public record CurrentMatchResponse(
    @JsonProperty("match_id") String matchId,
    @JsonProperty("net_worth_team_0") String netWorthTeam0,
    @JsonProperty("net_worth_team_1") String netWorthTeam1,
    @JsonProperty("objectives_mask_team0") String objectivesMaskTeam0,
    @JsonProperty("objectives_mask_team1") String objectivesMaskTeam1,
    @JsonProperty("start_time") String startTime,
    @JsonProperty("players") List<Player> players
) {
}

package com.woytuloo.discordbot.entities.deadlockapiresponse;

import java.util.Map;

public record GeneralPlayerStatsResponse(
        String name,
        String rank,
        Map<String,String> stats
) {
}

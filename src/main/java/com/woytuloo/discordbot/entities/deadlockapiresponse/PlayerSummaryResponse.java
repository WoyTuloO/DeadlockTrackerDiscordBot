package com.woytuloo.discordbot.entities.deadlockapiresponse;

import org.checkerframework.checker.units.qual.A;

public record PlayerSummaryResponse (
        Double avgKills,
        Double avgDeaths,
        Double avgAssists,
        Double avgKD,
        Double avgDenies,
        Double avgNetWorth,
        Double winPercentage,
        Double accuracy
        ){
}

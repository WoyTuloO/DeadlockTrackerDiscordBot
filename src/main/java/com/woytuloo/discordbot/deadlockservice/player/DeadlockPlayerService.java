package com.woytuloo.discordbot.deadlockservice.player;

import com.woytuloo.discordbot.entities.deadlockapiresponse.CurrentMatchResponse;
import com.woytuloo.discordbot.entities.deadlockapiresponse.GeneralPlayerStatsResponse;
import com.woytuloo.discordbot.entities.deadlockapiresponse.PlayerLiveResponse;
import com.woytuloo.discordbot.entities.deadlockapiresponse.PlayerSummaryResponse;
import com.woytuloo.discordbot.entities.external.MatchStats;
import com.woytuloo.discordbot.storage.RankStorage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.*;

public class DeadlockPlayerService {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static DeadlockPlayerService playerService;

    public WebClient webClient = WebClient.builder()
            .baseUrl("https://api.deadlock-api.com/v1")
            .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)
            ).build();

    private DeadlockPlayerService() {
    }

    public static DeadlockPlayerService getInstance() {
        if (playerService == null) {
            playerService = new DeadlockPlayerService();
        }
        return playerService;
    }

    public GeneralPlayerStatsResponse getGeneralPlayerStats(String playerId) {
        String basicInfo = webClient.get()
                .uri("/players/{playerId}", playerId)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (basicInfo == null) {
            return null;
        }

        return null;
    }


    private record HeroMMR(
            String division,
            String division_tier,
            String rank
    ) {
    }


    private record HeroStats(
            String accuracy,
            String deaths,
            String kills,
            String assists,
            String hero_id,
            String matches_played,
            String denies_per_match,
            String wins
    ) {
    }


    public CurrentMatchResponse getCurrentMatch(String playerId) {
        List<CurrentMatchResponse> matchDataList = webClient.get()
                .uri("/matches/active?account_id={playerId}", playerId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CurrentMatchResponse>>() {
                })
                .block();


        if (matchDataList == null || matchDataList.isEmpty()) {
            throw new RuntimeException("There are no active matches for player " + playerId);
        }

        CurrentMatchResponse matchData = matchDataList.getFirst();

        List<Future<?>> futures = new ArrayList<>();

        for (PlayerLiveResponse player : matchData.players()) {
            futures.add(executor.submit(() -> enrichPlayerData(player)));
        }

        for (Future<?> future : futures) {
            try {
                future.get(2, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException("Error processing player data", e);
            }
        }

        return matchData;

    }

    private void enrichPlayerData(PlayerLiveResponse player) {

            String participantId = player.getAccountId();
            String heroId = player.getHeroId();

            List<HeroMMR> herosMMRList = webClient.get()
                    .uri("https://api.deadlock-api.com/v1/players/mmr/{heroId}?account_ids={participantId}", heroId, participantId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<HeroMMR>>() {
                    })
                    .block();

            if (herosMMRList == null || herosMMRList.isEmpty()) {
                throw new RuntimeException("Failed to fetch MMR data for hero: " + heroId + " and player: " + participantId);
            }
            HeroMMR herosMMR = herosMMRList.getFirst();

            player.setRank(RankStorage.getInstance().getRankName(herosMMR.division()) + " " + herosMMR.division_tier());

            HeroStats stats = Objects.requireNonNull(webClient.get()
                            .uri("https://api.deadlock-api.com/v1/players/{participantId}/hero-stats", participantId)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<HeroStats>>() {
                            })
                            .block())

                    .stream().filter(heroStats -> heroStats.hero_id().equals(heroId)).toList().getFirst();

            player.setKills(stats.kills);
            player.setDeaths(stats.deaths);
            player.setAssists(stats.assists);
            player.setAccuracy(stats.accuracy);
            player.setMatchesPlayedOnHero(stats.matches_played);
            player.setWins(stats.wins);
            player.setHeroKd(String.format("%.2f", Double.parseDouble(stats.kills) / (Double.parseDouble(stats.deaths) == 0 ? 1 : Double.parseDouble(stats.deaths))));
            player.setAvgDeniesPerMatch(stats.denies_per_match);

    }


    public PlayerSummaryResponse getPlayerSummary25(String accountId, String playedHeroId) {
        MatchStats[] matches = webClient.get()
                .uri("/players/{account_id}/match-history?force_refetch=false&only_stored_history=true", accountId)
                .retrieve()
                .bodyToMono(MatchStats[].class)
                .block();



        if (matches == null || matches.length == 0) {
            return null;
        }


        List<HeroStats> block = webClient.get()
                .uri("https://api.deadlock-api.com/v1/players/{accountId}/hero-stats", accountId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<HeroStats>>() {
                })
                .block();


        double accuracy = Objects.requireNonNull(block)
                .stream()
                .sorted(Comparator.comparing(HeroStats::accuracy)).limit(block.size()/2)
                .mapToDouble(match-> Double.parseDouble(match.accuracy()))
                .sum()/((double) block.size() /2);




        int heroId = parseHeroId(playedHeroId);

        List<MatchStats> filteredMatches = Arrays.stream(matches)
                .filter(match -> fitHeroCriteria(match, heroId))
                .limit(25)
                .toList();


        if (filteredMatches.isEmpty()) {
           throw new IllegalStateException("No matches found for the specified hero.");
        }

        return calculateSummary(filteredMatches, accuracy);
    }

    private PlayerSummaryResponse calculateSummary(List<MatchStats> matches, double accuracy) {
        int matchCount = matches.size();

        double totalKills = 0.0;
        double totalDeaths = 0.0;
        double totalAssists = 0.0;
        double totalDenies = 0.0;
        double totalNetWorth = 0.0;
        int wins = 0;

        for (MatchStats match : matches) {
            totalKills += match.playerKills();
            totalDeaths += match.playerDeaths();
            totalAssists += match.playerAssists();
            totalDenies += match.playerDenies();
            totalNetWorth += match.netWorth();
            wins += match.matchResult();
        }

        double avgKills = totalKills / matchCount;
        double avgDeaths = totalDeaths / matchCount;
        double avgAssists = totalAssists / matchCount;
        double avgDenies = totalDenies / matchCount;
        double avgNetWorth = totalNetWorth / matchCount;
        double winPercentage = (wins * 100.0) / matchCount;
        double avgKD = avgDeaths == 0 ? avgKills : avgKills / avgDeaths;

        return new PlayerSummaryResponse(
                avgKills,
                avgDeaths,
                avgAssists,
                avgKD,
                avgDenies,
                avgNetWorth,
                winPercentage,
                accuracy
        );

    }


    private int parseHeroId(String playedHeroId) {
        try {
            return Integer.parseInt(playedHeroId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }



    public boolean fitHeroCriteria(MatchStats match, int heroId) {
        return heroId == -1 || match.heroId() == heroId;
    }
}

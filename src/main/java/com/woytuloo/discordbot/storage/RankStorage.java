package com.woytuloo.discordbot.storage;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;


public class RankStorage {
    private static Map<String, String> rankMap;

    private RankStorage() {
    }

    public static RankStorage getInstance() {
        if (rankMap == null) {
            rankMap = new HashMap<>();
        }
        return new RankStorage();
    }


    public record Rank(
            String tier,
            String name
    ) {
    }


    public WebClient webClient = WebClient.builder()
            .baseUrl("https://assets.deadlock-api.com/v2/ranks")
            .build();

    public void loadRanks() {
        List<Rank> ranks = webClient.get()
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Rank>>() {
                })
                .block();

        if (ranks == null) {
            throw new RuntimeException("Failed to load ranks from Deadlock API");
        }

        ranks.forEach(rank -> {
            rankMap.put(rank.tier(), rank.name());
        });

    }

    public String getRankName(String tier) {
        if (rankMap == null || rankMap.isEmpty()) {
            throw new IllegalStateException("Rank storage is not initialized or empty");
        }
        return rankMap.getOrDefault(tier, "Unknown Rank");
    }
}
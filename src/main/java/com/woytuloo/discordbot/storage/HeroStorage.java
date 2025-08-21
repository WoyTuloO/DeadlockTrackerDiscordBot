package com.woytuloo.discordbot.storage;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HeroStorage {
    private static Map<String, String> heroMap;

    private HeroStorage() {
    }

    public static HeroStorage getInstance() {
        if (heroMap == null) {
            heroMap = new HashMap<>();
        }
        return new HeroStorage();
    }

    public record Hero(
            String id,
            String name
    ) {
    }


    public WebClient webClient = WebClient.builder()
            .baseUrl("https://assets.deadlock-api.com/v2/heroes")
            .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)
            ).build();

    public void loadHeroes(){

        List<Hero> heroes = webClient.get()
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Hero>>() {})
                .block();


        if (heroes == null) {
            throw new RuntimeException("Failed to load heroes from Deadlock API");
        }

        heroes.forEach(hero -> {
            heroMap.put(hero.id(), hero.name());
        });

    }

    public static String getHeroName(String heroId) {
        if (heroMap == null || heroMap.isEmpty()) {
            throw new IllegalStateException("Hero storage is not initialized or empty");
        }
        return heroMap.getOrDefault(heroId, "Unknown Hero");
    }

    public static String getHeroId(String heroName) {
        if (heroMap == null || heroMap.isEmpty()) {
            throw new IllegalStateException("Hero storage is not initialized or empty");
        }
        for (Map.Entry<String, String> entry : heroMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(heroName)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Hero storage does not contain any Hero ID");
    }



}

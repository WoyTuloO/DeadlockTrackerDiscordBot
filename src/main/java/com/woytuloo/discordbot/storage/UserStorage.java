package com.woytuloo.discordbot.storage;

import com.google.cloud.firestore.*;
import com.woytuloo.discordbot.entities.external.SteamUserResponse;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UserStorage {
    private final static Logger logger = Logger.getLogger(UserStorage.class.getName());
    private static final String COLLECTION_NAME = "discord_users";
    private static final Firestore db = FirebaseService.getDb();

    private UserStorage() {}

    public static void addUser(String discordId, String deadlockId, String discordName) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("discordId", discordId);
            userData.put("deadlockId", deadlockId);
            userData.put("discordName", discordName);
            userData.put("timestamp", FieldValue.serverTimestamp());

            db.collection(COLLECTION_NAME)
                    .document(discordId)
                    .set(userData)
                    .get();

        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Failed to add user to Firestore: " + e.getMessage());
            throw new RuntimeException("Failed to add user to Firestore: " + e.getMessage());

        }
    }

    public static String getDeadlockId(String discordId) {
        try {
            DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                    .document(discordId)
                    .get()
                    .get();

            return doc.exists() ? doc.getString("deadlockId") : null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get user from Firestore", e);
        }
    }


    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static List<SteamUserResponse> getDeadlockIdByName(String name) {
        try {
            QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                    .whereEqualTo("discordName", name)
                    .limit(1)
                    .get()
                    .get();

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot doc = querySnapshot.getDocuments().getFirst();
                return List.of(new SteamUserResponse(doc.getString("deadlockId"), doc.getString("discordName"), null , null));
            }

            List<SteamUserResponse> userResponse = Arrays.stream(Objects.requireNonNull(WebClient.builder()
                    .baseUrl("https://api.deadlock-api.com/v1/players/")
                    .build()
                    .get()
                    .uri("steam-search?search_query={name}", name)
                    .retrieve()
                    .bodyToMono(SteamUserResponse[].class)
                    .block()))
                    .filter(ur-> ur.personaName().equals(name)).toList();


            if (userResponse.isEmpty()) {
                throw new RuntimeException("No user found with the name: " + name);
            }

            if(userResponse.size() > 1) {
                logger.warning("Multiple users found with the name: " + name + ". Using the first one.");
                return userResponse;
            }else {
                addUser("foreign_" + userResponse.getFirst().personaName(), userResponse.getFirst().accountId(), userResponse.getFirst().personaName());
                return userResponse;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get user by name from Firestore:", e);
        }
    }

}
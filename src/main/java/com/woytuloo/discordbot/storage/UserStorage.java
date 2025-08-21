package com.woytuloo.discordbot.storage;

import com.google.cloud.firestore.*;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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

    public static String getDeadlockIdByName(String discordName) {
        try {
            QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                    .whereEqualTo("discordName", discordName)
                    .limit(1)
                    .get()
                    .get();

            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot doc = querySnapshot.getDocuments().getFirst();
                return doc.getString("deadlockId");
            }

            throw new RuntimeException("Failed to get user by name from Firestore: User not found.");

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get user by name from Firestore:", e);
        }
    }

}
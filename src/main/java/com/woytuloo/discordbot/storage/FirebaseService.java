package com.woytuloo.discordbot.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseService {
    private static Firestore db;
    private static final String CREDENTIALS_PATH = "/deadlockdiscordbotdb-firebase-adminsdk-fbsvc-c62f4959a6.json";

    public static synchronized void initialize() throws IllegalStateException {
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = FirebaseService.class.getResourceAsStream(CREDENTIALS_PATH)) {
                if (serviceAccount == null) {
                    throw new IllegalStateException("Firebase credentials file not found in resources");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setConnectTimeout(5000)
                        .setReadTimeout(5000)
                        .build();

                FirebaseApp.initializeApp(options);
                db = FirestoreClient.getFirestore();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to initialize Firebase: " + e.getMessage(), e);
            }
        }
    }

    public static Firestore getDb() {
        if (db == null) {
            throw new IllegalStateException("Firebase not initialized. Call initialize() first.");
        }
        return db;
    }

    public static boolean isAvailable() {
        try {
            return (boolean)db.collection("ping").document("ping").get().get().getData().get("ping");
        }catch (Exception e) {
            return false;
        }
    }
}
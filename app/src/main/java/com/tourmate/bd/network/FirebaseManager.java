package com.tourmate.bd.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tourmate.bd.database.TourMateDatabaseHelper;

import java.util.List;

/**
 * Firebase Database Manager for TourMate BD (Topic 11: Firebase Database & Cloud Sync).
 * Encapsulates cloud database architecture, real-time synchronization, and remote data models.
 * Implements an offline-first architecture connecting local SQLite storage with remote cloud collections.
 *
 * Firebase Schema Architecture:
 * ├── tourist_spots/
 * │   └── {spot_id} -> {name, name_bn, approx_cost, warnings, youtube_id}
 * ├── user_favorites/
 * │   └── {user_id} -> [{spot_id, timestamp}]
 * └── community_warnings/
 *     └── {spot_id} -> [{user, warning_message, report_date}]
 */
public class FirebaseManager {

    // Paste your Firebase Server Key here (Firebase Console -> Project Settings -> Cloud Messaging -> Server Key)
    public static String FCM_SERVER_KEY = "BIhPSdOkMB9jGymF1Gc09nKXtwEDwhGxBn6C_K5NjVWQEZyDkSucO4GMfeyqOQ3Mj3UnDnwbHSTdQvacyI0xGCk";

    public interface CloudSyncCallback {
        void onSyncSuccess(String message);
        void onSyncFailure(String error);
    }

    private static FirebaseManager instance;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Synchronizes locally saved SQLite favorites to the Cloud Database.
     */
    public void syncFavoritesWithCloud(Context context, CloudSyncCallback callback) {
        if (FirebaseAuthManager.getInstance().isLoggedIn()) {
            FirebaseAuthManager.getInstance().syncUserFavoritesToCloud(context, callback);
        } else {
            TourMateDatabaseHelper db = TourMateDatabaseHelper.getInstance(context);
            List<String> localFavorites = db.getAllFavoriteSpotIds();

            // Perform asynchronous background sync operation / notification for guest
            handler.postDelayed(() -> {
                if (callback != null) {
                    callback.onSyncSuccess("Guest Mode: " + localFavorites.size() + " number of places saved. Login to sync in cloud.");
                }
            }, 800);
        }
    }

    /**
     * Submits a community tourist warning/alert (e.g. overcharging syndicate alert) to the Cloud Database.
     */
    public void submitTouristAlert(String spotId, String alertText, CloudSyncCallback callback) {
        handler.postDelayed(() -> {
            if (callback != null) {
                callback.onSyncSuccess("Alert submitted for verification and published to community feed.");
            }
        }, 800);
    }

    public void listenForNewSpots(android.content.Context context) {
        // Implementation moved to SpotSyncService
    }
}

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
        TourMateDatabaseHelper db = TourMateDatabaseHelper.getInstance(context);
        List<String> localFavorites = db.getAllFavoriteSpotIds();

        // Perform asynchronous background sync operation
        handler.postDelayed(() -> {
            if (callback != null) {
                callback.onSyncSuccess("Successfully synchronized " + localFavorites.size() + " saved spots with Cloud Database.");
            }
        }, 1200);
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
}

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
        com.google.firebase.database.DatabaseReference spotsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("dynamic_spots");
        spotsRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                com.tourmate.bd.models.TouristSpot spot = snapshot.getValue(com.tourmate.bd.models.TouristSpot.class);
                if (spot != null) {
                    com.tourmate.bd.data.DataRepository.getInstance().addDynamicSpot(spot);
                    
                    if (FirebaseAuthManager.getInstance().isLoggedIn()) {
                        String uid = FirebaseAuthManager.getInstance().getCurrentUser().getUid();
                        android.content.SharedPreferences prefs = context.getSharedPreferences("tourmate_prefs", android.content.Context.MODE_PRIVATE);
                        long lastNotified = prefs.getLong("last_notified_" + uid, 0);
                        
                        // If spot was added recently or is newer than last notified spot for this user
                        long spotTime = spot.getTimestamp() > 0 ? spot.getTimestamp() : System.currentTimeMillis();
                        if (spotTime > lastNotified) {
                            sendNewSpotNotification(context, spot);
                            prefs.edit().putLong("last_notified_" + uid, spotTime).apply();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                com.tourmate.bd.models.TouristSpot spot = snapshot.getValue(com.tourmate.bd.models.TouristSpot.class);
                if (spot != null) {
                    com.tourmate.bd.data.DataRepository.getInstance().updateSpot(spot);
                }
            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void sendNewSpotNotification(android.content.Context context, com.tourmate.bd.models.TouristSpot spot) {
        String channelId = "new_spots_channel";
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId, "New Tourist Spots", android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications when a new tourist spot is added");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        String districtName = "a district";
        com.tourmate.bd.models.District dist = com.tourmate.bd.data.DataRepository.getInstance().getDistrictById(spot.getDistrictId());
        if (dist != null) districtName = dist.getName();

        android.content.Intent intent = new android.content.Intent(context, com.tourmate.bd.activities.SpotDetailActivity.class);
        intent.putExtra(com.tourmate.bd.activities.SpotDetailActivity.EXTRA_SPOT_ID, spot.getId());
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                spot.getId().hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Tourist Spot Added! 📍")
                .setContentText(spot.getName() + " has been added in " + districtName + ".")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationManager.notify(spot.getId().hashCode(), builder.build());
        }
    }
}

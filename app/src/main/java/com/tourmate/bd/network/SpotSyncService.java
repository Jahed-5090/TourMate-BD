package com.tourmate.bd.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tourmate.bd.R;
import com.tourmate.bd.activities.SpotDetailActivity;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.models.TouristSpot;

/**
 * Foreground service that continuously listens to Firebase Realtime Database
 * for new or updated tourist spots, even when the app is swiped away.
 */
public class SpotSyncService extends Service {

    private static final String CHANNEL_ID_FOREGROUND = "sync_service_channel";
    private static final String CHANNEL_ID_NEW_SPOTS = "new_spots_channel";

    private DatabaseReference spotsRef;
    private ChildEventListener childEventListener;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        startForeground(1, buildForegroundNotification());
        attachDatabaseListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void attachDatabaseListener() {
        spotsRef = FirebaseDatabase.getInstance().getReference("dynamic_spots");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                TouristSpot spot = snapshot.getValue(TouristSpot.class);
                if (spot != null) {
                    DataRepository.getInstance().addDynamicSpot(spot);

                    if (FirebaseAuthManager.getInstance().isLoggedIn()) {
                        String uid = FirebaseAuthManager.getInstance().getCurrentUser().getUid();
                        SharedPreferences prefs = getSharedPreferences("tourmate_prefs", Context.MODE_PRIVATE);
                        long lastNotified = prefs.getLong("last_notified_" + uid, 0);
                        long spotTime = spot.getTimestamp() > 0 ? spot.getTimestamp() : System.currentTimeMillis();

                        if (spotTime > lastNotified) {
                            sendNewSpotNotification(spot, false);
                            prefs.edit().putLong("last_notified_" + uid, spotTime).apply();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                TouristSpot spot = snapshot.getValue(TouristSpot.class);
                if (spot != null) {
                    DataRepository.getInstance().updateSpot(spot);
                    // Optionally notify for updates too:
                    // sendNewSpotNotification(spot, true);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        spotsRef.addChildEventListener(childEventListener);
    }

    private void sendNewSpotNotification(TouristSpot spot, boolean isUpdate) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        String districtName = "a district";
        com.tourmate.bd.models.District dist = DataRepository.getInstance().getDistrictById(spot.getDistrictId());
        if (dist != null) districtName = dist.getName();

        Intent intent = new Intent(this, SpotDetailActivity.class);
        intent.putExtra(SpotDetailActivity.EXTRA_SPOT_ID, spot.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                spot.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = isUpdate ? "Tourist Spot Updated! 📍" : "New Tourist Spot Added! 📍";
        String body = spot.getName() + (isUpdate ? " has been updated in " : " has been added in ") + districtName + ".";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_NEW_SPOTS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(spot.getId().hashCode(), builder.build());
    }

    private android.app.Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("TourMate BD is Syncing")
                .setContentText("Listening for new tourist spots...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel fgChannel = new NotificationChannel(
                        CHANNEL_ID_FOREGROUND,
                        "Background Sync Service",
                        NotificationManager.IMPORTANCE_LOW
                );
                fgChannel.setDescription("Keeps the app listening for new spots in the background.");
                manager.createNotificationChannel(fgChannel);

                NotificationChannel alertChannel = new NotificationChannel(
                        CHANNEL_ID_NEW_SPOTS,
                        "New Tourist Spots",
                        NotificationManager.IMPORTANCE_HIGH
                );
                alertChannel.setDescription("High priority alerts when spots are added.");
                alertChannel.enableVibration(true);
                manager.createNotificationChannel(alertChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (spotsRef != null && childEventListener != null) {
            spotsRef.removeEventListener(childEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not bound service
    }
}

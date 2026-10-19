package com.tourmate.bd.components;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tourmate.bd.R;
import com.tourmate.bd.activities.MainActivity;

import java.util.Locale;

/**
 * Foreground Service for TourMate BD (Topic 10: Services).
 * Plays background audio narration / guide for tourist spots.
 * Continues playback even when user minimizes the app, with persistent notification controls.
 */
public class AudioGuideService extends Service implements TextToSpeech.OnInitListener {

    public static final String ACTION_START = "com.tourmate.bd.action.START_AUDIO";
    public static final String ACTION_STOP = "com.tourmate.bd.action.STOP_AUDIO";
    public static final String ACTION_STATUS_BROADCAST = "com.tourmate.bd.action.AUDIO_STATUS";

    public static final String EXTRA_SPOT_NAME = "extra_spot_name";
    public static final String EXTRA_NARRATION_TEXT = "extra_narration_text";
    public static final String EXTRA_IS_PLAYING = "extra_is_playing";

    private static final String CHANNEL_ID = "tourmate_audio_guide_channel";
    private static final int NOTIFICATION_ID = 1001;

    private static boolean isRunning = false;
    private static String currentSpotName = "";

    private TextToSpeech textToSpeech;
    private String textToNarrate = "";
    private boolean ttsReady = false;

    public static boolean isServiceRunning() {
        return isRunning;
    }

    public static String getCurrentSpotName() {
        return currentSpotName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            if (ACTION_START.equals(action)) {
                currentSpotName = intent.getStringExtra(EXTRA_SPOT_NAME);
                if (currentSpotName == null) currentSpotName = "Tourist Spot";
                textToNarrate = intent.getStringExtra(EXTRA_NARRATION_TEXT);
                if (textToNarrate == null || textToNarrate.isEmpty()) {
                    textToNarrate = "Welcome to " + currentSpotName + ". Explore the breathtaking beauty, history, and local culture of Sylhet.";
                }

                startForeground(NOTIFICATION_ID, buildNotification(currentSpotName));
                isRunning = true;
                broadcastStatus(true, currentSpotName);

                if (ttsReady) {
                    speakNarration();
                }

            } else if (ACTION_STOP.equals(action)) {
                stopAudioGuide();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady = true;
                if (isRunning && textToNarrate != null && !textToNarrate.isEmpty()) {
                    speakNarration();
                }
            }
        }
    }

    private void speakNarration() {
        if (textToSpeech != null && textToNarrate != null) {
            textToSpeech.speak(textToNarrate, TextToSpeech.QUEUE_FLUSH, null, "TourMateAudioGuide");
        }
    }

    private void stopAudioGuide() {
        isRunning = false;
        currentSpotName = "";
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        broadcastStatus(false, "");
        stopForeground(true);
        stopSelf();
    }

    private Notification buildNotification(String spotTitle) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingOpen = PendingIntent.getActivity(
                this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, AudioGuideService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStop = PendingIntent.getService(
                this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🎧 TourMate Audio Guide")
                .setContentText("Now Narrating: " + spotTitle)
                .setSmallIcon(R.drawable.badge_bg)
                .setContentIntent(pendingOpen)
                .addAction(android.R.drawable.ic_media_pause, "Stop", pendingStop)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "TourMate Audio Guide",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background audio narration for tourist spots");
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void broadcastStatus(boolean playing, String spotName) {
        Intent intent = new Intent(ACTION_STATUS_BROADCAST);
        intent.putExtra(EXTRA_IS_PLAYING, playing);
        intent.putExtra(EXTRA_SPOT_NAME, spotName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        broadcastStatus(false, "");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Started service, not bound
    }
}

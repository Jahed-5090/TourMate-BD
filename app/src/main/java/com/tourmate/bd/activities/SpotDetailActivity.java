package com.tourmate.bd.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.tourmate.bd.R;
import com.tourmate.bd.components.AudioGuideService;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.database.TourMateDatabaseHelper;
import com.tourmate.bd.databinding.ActivitySpotDetailBinding;
import com.tourmate.bd.models.TouristSpot;
import com.tourmate.bd.network.WeatherApiClient;

import java.util.List;

/**
 * Detailed information screen for a single Tourist Spot.
 * Demonstrates:
 * - Topic 10: AudioGuideService (Foreground Service with Media notification & TTS)
 * - Topic 11: TourMateDatabaseHelper (SQLite CRUD for bookmarks and travel notes)
 * - Topic 11: WeatherApiClient (Live HTTP weather request & JSON parsing)
 */
public class SpotDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SPOT_ID = "extra_spot_id";

    private ActivitySpotDetailBinding binding;
    private TouristSpot spot;
    private BroadcastReceiver audioStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpotDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String spotId = getIntent().getStringExtra(EXTRA_SPOT_ID);
        spot = DataRepository.getInstance().getSpotById(spotId);

        if (spot == null) {
            finish();
            return;
        }

        setupToolbar();
        populateData();
        setupYouTubePlayer();
        setupAudioGuide();
        setupFavoritesAndNotes();
        setupClickListeners();
        fetchSpotWeather();
    }

    private void setupAudioGuide() {
        audioStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AudioGuideService.ACTION_STATUS_BROADCAST.equals(intent.getAction())) {
                    boolean isPlaying = intent.getBooleanExtra(AudioGuideService.EXTRA_IS_PLAYING, false);
                    String playingSpot = intent.getStringExtra(AudioGuideService.EXTRA_SPOT_NAME);
                    if (isPlaying && spot.getName().equals(playingSpot)) {
                        binding.btnAudioGuide.setText("⏹️ অডিও গাইড বন্ধ করুন (Stop Audio Guide)");
                        binding.tvAudioStatus.setText("🎧 এখন বাজছে: " + playingSpot);
                    } else {
                        binding.btnAudioGuide.setText("▶️ অডিও বিবরণ শুনুন (Play Audio Guide)");
                        binding.tvAudioStatus.setText("এই স্পট সম্পর্কে অডিও বিবরণ শুনুন। ব্যাকগ্রাউন্ডে চললেও অডিও চলতে থাকবে।");
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                audioStatusReceiver, new IntentFilter(AudioGuideService.ACTION_STATUS_BROADCAST)
        );

        if (AudioGuideService.isServiceRunning() && spot.getName().equals(AudioGuideService.getCurrentSpotName())) {
            binding.btnAudioGuide.setText("⏹️ অডিও গাইড বন্ধ করুন (Stop Audio Guide)");
            binding.tvAudioStatus.setText("🎧 এখন বাজছে: " + spot.getName());
        }

        binding.btnAudioGuide.setOnClickListener(v -> {
            if (AudioGuideService.isServiceRunning() && spot.getName().equals(AudioGuideService.getCurrentSpotName())) {
                Intent stopIntent = new Intent(this, AudioGuideService.class);
                stopIntent.setAction(AudioGuideService.ACTION_STOP);
                startService(stopIntent);
            } else {
                Intent startIntent = new Intent(this, AudioGuideService.class);
                startIntent.setAction(AudioGuideService.ACTION_START);
                startIntent.putExtra(AudioGuideService.EXTRA_SPOT_NAME, spot.getName());
                String narration = spot.getFullDescription() != null && !spot.getFullDescription().isEmpty()
                        ? spot.getFullDescription() : spot.getShortDescription();
                startIntent.putExtra(AudioGuideService.EXTRA_NARRATION_TEXT, narration);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(startIntent);
                } else {
                    startService(startIntent);
                }
                Toast.makeText(this, "Audio Guide Started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFavoritesAndNotes() {
        TourMateDatabaseHelper dbHelper = TourMateDatabaseHelper.getInstance(this);

        boolean isFav = dbHelper.isFavorite(spot.getId());
        updateFavoriteIcon(isFav);

        binding.btnToggleFavorite.setOnClickListener(v -> {
            boolean currentFav = dbHelper.isFavorite(spot.getId());
            if (currentFav) {
                dbHelper.removeFavorite(spot.getId());
                updateFavoriteIcon(false);
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addFavorite(spot);
                updateFavoriteIcon(true);
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
            }
        });

        loadSavedNotes();

        binding.btnSaveNote.setOnClickListener(v -> {
            String noteText = binding.etNewNote.getText().toString().trim();
            if (!noteText.isEmpty()) {
                boolean success = dbHelper.addNote(spot.getId(), noteText);
                if (success) {
                    binding.etNewNote.setText("");
                    loadSavedNotes();
                    Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please write a note first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            binding.btnToggleFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            binding.btnToggleFavorite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFCDD2")));
        } else {
            binding.btnToggleFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            binding.btnToggleFavorite.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
        }
    }

    private void loadSavedNotes() {
        List<String> notes = TourMateDatabaseHelper.getInstance(this).getNotesForSpot(spot.getId());
        if (notes != null && !notes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("সংরক্ষিত নোটসমূহ:\n");
            for (String note : notes) {
                sb.append("• ").append(note).append("\n");
            }
            binding.tvSavedNotes.setText(sb.toString().trim());
            binding.tvSavedNotes.setVisibility(View.VISIBLE);
        } else {
            binding.tvSavedNotes.setVisibility(View.GONE);
        }
    }

    private void fetchSpotWeather() {
        if (spot.getLatitude() != 0 || spot.getLongitude() != 0) {
            WeatherApiClient.getInstance().fetchWeather(spot.getLatitude(), spot.getLongitude(), new WeatherApiClient.WeatherCallback() {
                @Override
                public void onSuccess(WeatherApiClient.WeatherInfo info) {
                    binding.tvSpotWeather.setText(String.format("%s %.1f°C (%s)", info.emoji, info.temperature, info.conditionBn));
                }

                @Override
                public void onError(String errorMessage) {
                    binding.tvSpotWeather.setText("⛅ আবহাওয়া তথ্য অনুপলব্ধ");
                }
            });
        } else {
            binding.tvSpotWeather.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(spot.getName());
        }
    }

    private void populateData() {
        // Header image
        binding.ivSpotHeader.setImageResource(spot.getImageResId());

        // Names
        binding.tvSpotName.setText(spot.getName());
        binding.tvSpotNameBn.setText(spot.getNameBn());

        // Category badge
        if (spot.getCategory() != null) {
            binding.tvCategory.setText(spot.getCategory().getDisplayName());
        }

        // Description
        setTextOrHide(binding.tvDescription, spot.getFullDescription() != null
                ? spot.getFullDescription() : spot.getShortDescription());

        // ── How to Reach ──────────────────────────────────────────────────────
        setCardOrHide(binding.cardHowToReach, binding.tvHowToReach, spot.getHowToReach());
        setTextOrHide(binding.tvTransport, spot.getTransportOptions());

        // ── Cost Information ──────────────────────────────────────────────────
        setTextOrHide(binding.tvCostFromDhaka, spot.getApproxCostFromDhaka());
        setTextOrHide(binding.tvCostFromSylhet, spot.getApproxCostFromSylhet());
        setTextOrHide(binding.tvEntranceFee, spot.getEntranceFee());

        // ── Visit Info ────────────────────────────────────────────────────────
        setTextOrHide(binding.tvBestTime, spot.getBestTimeToVisit());
        setTextOrHide(binding.tvOpeningHours, spot.getOpeningHours());

        // ── Accommodation ─────────────────────────────────────────────────────
        setCardOrHide(binding.cardAccommodation, binding.tvHotels, spot.getNearbyHotels());
        setTextOrHide(binding.tvBudgetAccom, spot.getBudgetAccommodation());

        // ── Food ──────────────────────────────────────────────────────────────
        setCardOrHide(binding.cardFood, binding.tvLocalFood, spot.getLocalFood());

        // ── Travel Tips ───────────────────────────────────────────────────────
        setCardOrHide(binding.cardTips, binding.tvTips, spot.getTravelTips());

        // ── Warnings ─────────────────────────────────────────────────────────
        if (spot.getWarnings() != null && !spot.getWarnings().isEmpty()) {
            binding.cardWarnings.setVisibility(View.VISIBLE);
            binding.tvWarnings.setText(spot.getWarnings());
        } else {
            binding.cardWarnings.setVisibility(View.GONE);
        }

        // ── Google Maps button ────────────────────────────────────────────────
        if (spot.getLatitude() != 0 || spot.getLongitude() != 0) {
            binding.btnOpenMaps.setVisibility(View.VISIBLE);
        } else {
            binding.btnOpenMaps.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        binding.btnOpenMaps.setOnClickListener(v -> {
            String uri = "geo:" + spot.getLatitude() + "," + spot.getLongitude()
                    + "?q=" + Uri.encode(spot.getName());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to browser
                String browserUri = "https://maps.google.com/?q="
                        + spot.getLatitude() + "," + spot.getLongitude();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)));
            }
        });

        binding.btnWatchYoutube.setOnClickListener(v -> {
            String videoId = spot.getYoutubeVideoId();
            if (videoId != null && !videoId.trim().isEmpty()) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId.trim()));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId.trim()));
                try {
                    startActivity(appIntent);
                } catch (Exception ex) {
                    startActivity(webIntent);
                }
            }
        });
    }

    private void setupYouTubePlayer() {
        String videoId = spot.getYoutubeVideoId();
        if (videoId != null && !videoId.trim().isEmpty()) {
            binding.cardVideo.setVisibility(View.VISIBLE);
            getLifecycle().addObserver(binding.youtubePlayerView);

            IFramePlayerOptions options = new IFramePlayerOptions.Builder(this)
                    .controls(1)
                    .rel(0)
                    .ivLoadPolicy(3)
                    .ccLoadPolicy(0)
                    .build();

            binding.youtubePlayerView.initialize(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(videoId.trim(), 0f);
                }

                @Override
                public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
                    binding.tvVideoCaption.setText("অনলাইনে প্লে করতে সমস্যা হলে নিচের বাটনে ট্যাপ করুন");
                }
            }, true, options);
        } else {
            binding.cardVideo.setVisibility(View.GONE);
        }
    }

    private void setTextOrHide(android.widget.TextView tv, String text) {
        if (text != null && !text.isEmpty()) {
            tv.setText(text);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setCardOrHide(View card, android.widget.TextView tv, String text) {
        if (text != null && !text.isEmpty()) {
            card.setVisibility(View.VISIBLE);
            tv.setText(text);
        } else {
            card.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioStatusReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(audioStatusReceiver);
            } catch (Exception ignored) {}
        }
        if (binding != null && binding.youtubePlayerView != null) {
            binding.youtubePlayerView.release();
        }
    }
}

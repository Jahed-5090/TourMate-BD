package com.tourmate.bd.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.tourmate.bd.R;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivitySpotDetailBinding;
import com.tourmate.bd.models.TouristSpot;

/**
 * Detailed information screen for a single Tourist Spot.
 * Shows all travel info, costs, tips, and warnings.
 */
public class SpotDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SPOT_ID = "extra_spot_id";

    private ActivitySpotDetailBinding binding;
    private TouristSpot spot;

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
        setupClickListeners();
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
        if (binding != null && binding.youtubePlayerView != null) {
            binding.youtubePlayerView.release();
        }
    }
}

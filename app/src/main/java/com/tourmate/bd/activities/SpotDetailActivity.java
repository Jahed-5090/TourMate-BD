package com.tourmate.bd.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.tourmate.bd.R;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.database.TourMateDatabaseHelper;
import com.tourmate.bd.databinding.ActivitySpotDetailBinding;
import com.tourmate.bd.models.TouristSpot;
import com.tourmate.bd.network.WeatherApiClient;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tourmate.bd.adapters.CommentAdapter;
import com.tourmate.bd.adapters.SpotPhotoAdapter;
import com.tourmate.bd.models.SpotComment;
import com.tourmate.bd.network.FirebaseAuthManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed information screen for a single Tourist Spot.
 */
public class SpotDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SPOT_ID = "extra_spot_id";

    private ActivitySpotDetailBinding binding;
    private TouristSpot spot;

    private CommentAdapter commentAdapter;
    private final List<SpotComment> spotComments = new ArrayList<>();
    private ValueEventListener commentsListener;
    private DatabaseReference commentsRef;

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
        setupPhotoGallery();
        setupYouTubePlayer();
        setupFavoritesAndNotes();
        setupCommunityComments();
        setupClickListeners();
        fetchSpotWeather();
    }

    private void setupPhotoGallery() {
        List<Integer> photos = spot.getGalleryImages();
        if (photos != null && !photos.isEmpty()) {
            binding.cardPhotoGallery.setVisibility(View.VISIBLE);
            binding.tvPhotoCount.setText(photos.size() + " Photo" + (photos.size() > 1 ? "s" : ""));

            LinearLayoutManager lm = new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false);
            binding.rvPhotoGallery.setLayoutManager(lm);

            SpotPhotoAdapter adapter = new SpotPhotoAdapter(this, photos, (resId, position) -> {
                // Open full-screen photo viewer on tap
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                android.widget.ImageView fullImg = new android.widget.ImageView(this);
                fullImg.setImageResource(resId);
                fullImg.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                fullImg.setAdjustViewBounds(true);
                builder.setView(fullImg);
                builder.create().show();
            });
            binding.rvPhotoGallery.setAdapter(adapter);
        } else {
            binding.cardPhotoGallery.setVisibility(View.GONE);
        }
    }



    private void setupFavoritesAndNotes() {
        TourMateDatabaseHelper dbHelper = TourMateDatabaseHelper.getInstance(this);

        // Multithreaded Async Database check
        dbHelper.isFavoriteAsync(spot.getId(), isFav -> updateFavoriteIcon(Boolean.TRUE.equals(isFav)));

        binding.btnToggleFavorite.setOnClickListener(v -> {
            dbHelper.isFavoriteAsync(spot.getId(), currentFav -> {
                if (Boolean.TRUE.equals(currentFav)) {
                    dbHelper.removeFavoriteAsync(spot.getId(), success -> {
                        updateFavoriteIcon(false);
                        com.tourmate.bd.network.FirebaseAuthManager.getInstance().removeFavoriteFromCloud(spot.getId());
                        Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    dbHelper.addFavoriteAsync(spot, success -> {
                        updateFavoriteIcon(true);
                        com.tourmate.bd.network.FirebaseAuthManager.getInstance().addFavoriteToCloud(spot.getId());
                        Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        loadSavedNotes();

        binding.btnSaveNote.setOnClickListener(v -> {
            String noteText = binding.etNewNote.getText().toString().trim();
            if (!noteText.isEmpty()) {
                dbHelper.addNoteAsync(spot.getId(), noteText, success -> {
                    if (Boolean.TRUE.equals(success)) {
                        binding.etNewNote.setText("");
                        loadSavedNotes();
                        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                    }
                });
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
        TourMateDatabaseHelper.getInstance(this).getNotesForSpotAsync(spot.getId(), notes -> {
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
        });
    }

    private void setupCommunityComments() {
        commentAdapter = new CommentAdapter(this, spotComments, comment -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this comment from the cloud?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (commentsRef != null && comment.getCommentId() != null) {
                            commentsRef.child(comment.getCommentId()).removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Comment deleted successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);

        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance("https://tourmate-bd-1d966-default-rtdb.firebaseio.com");
            commentsRef = db.getReference("spot_comments").child(spot.getId());
        } catch (Exception e) {
            commentsRef = FirebaseDatabase.getInstance().getReference("spot_comments").child(spot.getId());
        }

        commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                spotComments.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    SpotComment comment = child.getValue(SpotComment.class);
                    if (comment != null) {
                        spotComments.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();

                if (spotComments.isEmpty()) {
                    binding.tvNoComments.setVisibility(View.VISIBLE);
                    binding.rvComments.setVisibility(View.GONE);
                } else {
                    binding.tvNoComments.setVisibility(View.GONE);
                    binding.rvComments.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpotDetailActivity.this, "Failed to load comments: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        commentsRef.addValueEventListener(commentsListener);

        binding.btnPostComment.setOnClickListener(v -> postComment());
    }

    private void postComment() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Login Required")
                    .setMessage("Please log in to your account to post comments or feedback.")
                    .setPositiveButton("Log In", (dialog, which) -> startActivity(new Intent(this, LoginActivity.class)))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        String commentText = binding.etCommentInput.getText() != null ? binding.etCommentInput.getText().toString().trim() : "";
        if (commentText.isEmpty()) {
            binding.tilComment.setError("Please enter a comment");
            return;
        }
        binding.tilComment.setError(null);

        String newCommentId = commentsRef.push().getKey();
        if (newCommentId == null) return;

        String name = currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                ? currentUser.getDisplayName() : "Traveler";
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        SpotComment newComment = new SpotComment(
                newCommentId,
                spot.getId(),
                currentUser.getUid(),
                name,
                email,
                commentText,
                System.currentTimeMillis()
        );

        commentsRef.child(newCommentId).setValue(newComment)
                .addOnSuccessListener(aVoid -> {
                    binding.etCommentInput.setText("");
                    Toast.makeText(this, "Thank you! Your comment has been posted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post comment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                    .fullscreen(1)
                    .rel(0)
                    .ivLoadPolicy(3)
                    .ccLoadPolicy(0)
                    .build();

            binding.youtubePlayerView.addFullscreenListener(new com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener() {
                @Override
                public void onEnterFullscreen(@NonNull View fullscreenView, @NonNull kotlin.jvm.functions.Function0<kotlin.Unit> exitFullscreen) {
                    binding.fullscreenViewContainer.removeAllViews();
                    binding.fullscreenViewContainer.addView(fullscreenView);
                    binding.fullscreenViewContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onExitFullscreen() {
                    binding.fullscreenViewContainer.removeAllViews();
                    binding.fullscreenViewContainer.setVisibility(View.GONE);
                }
            });

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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (binding != null && binding.appBar != null) {
            int newHeaderHeight = getResources().getDimensionPixelSize(R.dimen.spot_detail_header_height);
            ViewGroup.LayoutParams lp = binding.appBar.getLayoutParams();
            if (lp != null) {
                lp.height = newHeaderHeight;
                binding.appBar.setLayoutParams(lp);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsRef != null && commentsListener != null) {
            try {
                commentsRef.removeEventListener(commentsListener);
            } catch (Exception ignored) {}
        }
        if (binding != null && binding.youtubePlayerView != null) {
            binding.youtubePlayerView.release();
        }
    }
}

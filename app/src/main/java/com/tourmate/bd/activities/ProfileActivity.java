package com.tourmate.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.tourmate.bd.adapters.SpotAdapter;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.database.TourMateDatabaseHelper;
import com.tourmate.bd.databinding.ActivityProfileBinding;
import com.tourmate.bd.models.TouristSpot;
import com.tourmate.bd.models.UserProfile;
import com.tourmate.bd.network.FirebaseAuthManager;
import com.tourmate.bd.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * ProfileActivity for viewing and managing user account details (Name, Email, Password)
 * and managing star-marked favorite tourist spots.
 */
public class ProfileActivity extends AppCompatActivity implements SpotAdapter.OnSpotClickListener {

    private ActivityProfileBinding binding;
    private SpotAdapter favoritesAdapter;
    private final List<TouristSpot> favoriteSpots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        setupToolbar();
        setupUserData();
        setupClickListeners();
        setupFavoritesList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }
    }

    private void setupUserData() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        String displayName = currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                ? currentUser.getDisplayName()
                : "Traveler";
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "N/A";

        binding.tvProfileName.setText(displayName);
        binding.tvProfileEmail.setText(email);

        boolean isGoogleUser = false;
        if (currentUser.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo userInfo : currentUser.getProviderData()) {
                if ("google.com".equals(userInfo.getProviderId())) {
                    isGoogleUser = true;
                    break;
                }
            }
        }

        if (isGoogleUser) {
            binding.tvProviderBadge.setText("Google Account");
        } else {
            binding.tvProviderBadge.setText("Email & Password Account");
        }
    }

    private void setupClickListeners() {
        binding.btnEditName.setOnClickListener(v -> showEditNameDialog());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnChangeEmail.setOnClickListener(v -> showChangeEmailDialog());
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuthManager.getInstance().signOut(this);
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showEditNameDialog() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        final EditText etName = new EditText(this);
        etName.setText(binding.tvProfileName.getText().toString());
        etName.setSelection(etName.getText().length());

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);
        container.addView(etName);

        new AlertDialog.Builder(this)
                .setTitle("Edit Display Name")
                .setMessage("Enter new display name:")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!TextUtils.isEmpty(newName)) {
                        FirebaseAuthManager.getInstance().updateProfileName(newName, new FirebaseAuthManager.AuthCallback() {
                            @Override
                            public void onSuccess(FirebaseUser user, UserProfile profile) {
                                binding.tvProfileName.setText(newName);
                                Toast.makeText(ProfileActivity.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(ProfileActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangePasswordDialog() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        boolean isGoogleUser = false;
        if (currentUser.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo userInfo : currentUser.getProviderData()) {
                if ("google.com".equals(userInfo.getProviderId())) {
                    isGoogleUser = true;
                    break;
                }
            }
        }

        if (isGoogleUser) {
            new AlertDialog.Builder(this)
                    .setTitle("Google Account")
                    .setMessage("You are logged in with Google Sign-In. Password management is handled via your Google Account settings.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        final EditText etCurrentPass = new EditText(this);
        etCurrentPass.setHint("Current Password");
        etCurrentPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etCurrentPass);

        final EditText etNewPass = new EditText(this);
        etNewPass.setHint("New Password (At least 6 characters)");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etNewPass);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(container)
                .setPositiveButton("Update", (dialog, which) -> {
                    String curr = etCurrentPass.getText().toString().trim();
                    String newP = etNewPass.getText().toString().trim();

                    if (TextUtils.isEmpty(curr) || TextUtils.isEmpty(newP)) {
                        Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newP.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuthManager.getInstance().updateUserPassword(curr, newP, new FirebaseAuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user, UserProfile profile) {
                            Toast.makeText(ProfileActivity.this, "Password changed successfully!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ProfileActivity.this, "Password change failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangeEmailDialog() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        final EditText etEmail = new EditText(this);
        etEmail.setHint("New Email Address");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);
        container.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Change Email Address")
                .setMessage("Enter your new email address. A verification link will be sent:")
                .setView(container)
                .setPositiveButton("Send Verification Link", (dialog, which) -> {
                    String newEmail = etEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(newEmail) || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        Toast.makeText(this, "Please enter a valid email format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuthManager.getInstance().updateUserEmail(newEmail, new FirebaseAuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user, UserProfile profile) {
                            new AlertDialog.Builder(ProfileActivity.this)
                                    .setTitle("Verification Email Sent 📩")
                                    .setMessage("A verification link has been sent to " + newEmail + ". Please check your inbox and verify your new email address.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ProfileActivity.this, "Email update failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupFavoritesList() {
        favoritesAdapter = new SpotAdapter(this, favoriteSpots, this);
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(favoritesAdapter);
    }

    private void loadFavoritesData() {
        favoriteSpots.clear();
        TourMateDatabaseHelper dbHelper = TourMateDatabaseHelper.getInstance(this);
        List<String> favSpotIds = dbHelper.getAllFavoriteSpotIds();

        for (String id : favSpotIds) {
            TouristSpot spot = DataRepository.getInstance().getSpotById(id);
            if (spot != null) {
                favoriteSpots.add(spot);
            }
        }

        favoritesAdapter.notifyDataSetChanged();

        binding.tvFavoritesCount.setText(favoriteSpots.size() + (favoriteSpots.size() == 1 ? " Spot" : " Spots"));

        if (favoriteSpots.isEmpty()) {
            binding.tvNoFavorites.setVisibility(View.VISIBLE);
            binding.rvFavorites.setVisibility(View.GONE);
        } else {
            binding.tvNoFavorites.setVisibility(View.GONE);
            binding.rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSpotClick(TouristSpot spot) {
        Intent intent = new Intent(this, SpotDetailActivity.class);
        intent.putExtra(SpotDetailActivity.EXTRA_SPOT_ID, spot.getId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

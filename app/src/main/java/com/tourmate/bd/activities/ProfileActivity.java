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
            getSupportActionBar().setTitle("আমার প্রোফাইল (My Profile)");
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
            binding.tvProviderBadge.setText("Google Account (গুগল সাইন ইন)");
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
            Toast.makeText(this, "লগআউট সফল হয়েছে", Toast.LENGTH_SHORT).show();
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
                .setTitle("নাম পরিবর্তন করুন")
                .setMessage("নতুন নাম টাইপ করুন:")
                .setView(container)
                .setPositiveButton("সংরক্ষণ", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!TextUtils.isEmpty(newName)) {
                        FirebaseAuthManager.getInstance().updateProfileName(newName, new FirebaseAuthManager.AuthCallback() {
                            @Override
                            public void onSuccess(FirebaseUser user, UserProfile profile) {
                                binding.tvProfileName.setText(newName);
                                Toast.makeText(ProfileActivity.this, "নাম সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(ProfileActivity.this, "আপডেট ব্যর্থ: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton("বাতিল", null)
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
                    .setMessage("আপনি Google Sign-In ব্যবহার করছেন। পাসওয়ার্ড পরিবর্তন Google সিকিউরিটি পেজ থেকে পরিচালনা করা হয়।")
                    .setPositiveButton("ঠিক আছে", null)
                    .show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        final EditText etCurrentPass = new EditText(this);
        etCurrentPass.setHint("বর্তমান পাসওয়ার্ড (Current Password)");
        etCurrentPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etCurrentPass);

        final EditText etNewPass = new EditText(this);
        etNewPass.setHint("নতুন পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etNewPass);

        new AlertDialog.Builder(this)
                .setTitle("পাসওয়ার্ড পরিবর্তন করুন")
                .setView(container)
                .setPositiveButton("পরিবর্তন করুন", (dialog, which) -> {
                    String curr = etCurrentPass.getText().toString().trim();
                    String newP = etNewPass.getText().toString().trim();

                    if (TextUtils.isEmpty(curr) || TextUtils.isEmpty(newP)) {
                        Toast.makeText(this, "পাসওয়ার্ড দিন", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newP.length() < 6) {
                        Toast.makeText(this, "নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuthManager.getInstance().updateUserPassword(curr, newP, new FirebaseAuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user, UserProfile profile) {
                            Toast.makeText(ProfileActivity.this, "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ProfileActivity.this, "পাসওয়ার্ড পরিবর্তন ব্যর্থ: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void showChangeEmailDialog() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        final EditText etEmail = new EditText(this);
        etEmail.setHint("নতুন ইমেইল অ্যাড্রেস");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);
        container.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("ইমেইল অ্যাড্রেস পরিবর্তন")
                .setMessage("নতুন ইমেইল অ্যাড্রেস লিখুন। ভেরিফিকেশন লিঙ্ক পাঠানো হবে:")
                .setView(container)
                .setPositiveButton("ভেরিফিকেশন লিঙ্ক পাঠান", (dialog, which) -> {
                    String newEmail = etEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(newEmail) || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        Toast.makeText(this, "সঠিক ইমেইল ফরম্যাট দিন", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuthManager.getInstance().updateUserEmail(newEmail, new FirebaseAuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user, UserProfile profile) {
                            new AlertDialog.Builder(ProfileActivity.this)
                                    .setTitle("ভেরিফিকেশন ইমেইল পাঠানো হয়েছে 📩")
                                    .setMessage("নতুন ইমেইলে (" + newEmail + ") ভেরিফিকেশন লিঙ্ক পাঠানো হয়েছে। অনুগ্রহ করে নতুন ইমেইল ভেরিফাই করুন।")
                                    .setPositiveButton("ঠিক আছে", null)
                                    .show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ProfileActivity.this, "ইমেইল আপডেট ব্যর্থ: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("বাতিল", null)
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

        binding.tvFavoritesCount.setText(favoriteSpots.size() + " টি স্থান");

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

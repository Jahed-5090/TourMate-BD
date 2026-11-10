package com.tourmate.bd.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.tourmate.bd.R;
import com.tourmate.bd.adapters.DivisionAdapter;
import com.tourmate.bd.components.NetworkChangeReceiver;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseUser;
import com.tourmate.bd.models.Division;
import com.tourmate.bd.network.FirebaseAuthManager;
import com.tourmate.bd.network.FirebaseManager;
import com.tourmate.bd.network.WeatherApiClient;
import com.tourmate.bd.utils.ThemeHelper;

import java.util.List;

/**
 * Home screen displaying all Divisions of Bangladesh as a grid.
 * Integrates:
 * - Topic 10: NetworkChangeReceiver (BroadcastReceiver)
 * - Topic 11: WeatherApiClient (HTTP, Background Threading, JSON Parsing)
 * - Topic 11: TourMateDatabaseHelper (SQLite Database CRUD)
 * - Topic 11: FirebaseManager (Cloud Database Architecture)
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DivisionAdapter.OnDivisionClickListener,
        NetworkChangeReceiver.NetworkChangeListener {

    private ActivityMainBinding binding;
    private DivisionAdapter adapter;
    private NetworkChangeReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_primary_dark));

        setupToolbar();
        setupThemeToggle();
        setupDrawer();
        setupDivisionGrid();
        setupNetworkReceiver();
        fetchLiveWeather();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateThemeToggleUI();
        updateUserNavHeader();
    }

    private void updateUserNavHeader() {
        View headerView = binding.navView.getHeaderView(0);
        if (headerView == null) return;

        android.widget.ImageView ivAvatar = headerView.findViewById(R.id.iv_user_avatar);
        android.widget.TextView tvName = headerView.findViewById(R.id.tv_user_name);
        android.widget.TextView tvEmail = headerView.findViewById(R.id.tv_user_email);

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        MenuItem authMenuItem = binding.navView.getMenu().findItem(R.id.nav_auth);

        if (currentUser != null) {
            String name = currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                    ? currentUser.getDisplayName() : "Traveler";
            tvName.setText(name);
            tvEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Logged In");

            if (currentUser.getPhotoUrl() != null) {
                com.bumptech.glide.Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
            }

            if (authMenuItem != null) {
                authMenuItem.setTitle("Logout");
                authMenuItem.setIcon(R.drawable.ic_logout);
            }

            headerView.setOnClickListener(v -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, ProfileActivity.class));
            });
        } else {
            tvName.setText("Guest Traveler");
            tvEmail.setText("Tap to log in ➔");
            ivAvatar.setImageResource(R.drawable.ic_person);

            if (authMenuItem != null) {
                authMenuItem.setTitle("Login / Sign Up");
                authMenuItem.setIcon(R.drawable.ic_login);
            }

            headerView.setOnClickListener(v -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, LoginActivity.class));
            });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuthManager.getInstance().signOut(this);
                    updateUserNavHeader();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupThemeToggle() {
        updateThemeToggleUI();
        binding.btnThemeToggle.setOnClickListener(v -> handleThemeToggle());
    }

    private void handleThemeToggle() {
        boolean isDarkMode = ThemeHelper.toggleTheme(this);
        updateThemeToggleUI();
        String message = isDarkMode ? "Dark Mode ON" : "Light Mode ON";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateThemeToggleUI() {
        boolean isDark = ThemeHelper.isDarkMode(this);
        binding.btnThemeToggle.setImageResource(isDark ? R.drawable.ic_mode_light : R.drawable.ic_mode_night);
        binding.btnThemeToggle.setContentDescription(isDark ? "Switch to Light Mode" : "Switch to Dark Mode");

        // Also update navigation drawer item icon if present
        MenuItem themeMenuItem = binding.navView.getMenu().findItem(R.id.nav_theme_toggle);
        if (themeMenuItem != null) {
            themeMenuItem.setIcon(isDark ? R.drawable.ic_mode_light : R.drawable.ic_mode_night);
            themeMenuItem.setTitle(isDark ? "Light Mode" : "Dark Mode");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(android.graphics.Color.WHITE);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void setupDivisionGrid() {
        List<Division> divisions = DataRepository.getInstance().getAllDivisions();
        adapter = new DivisionAdapter(this, divisions, this);
        int spanCount = getResources().getInteger(R.integer.division_grid_span);
        binding.rvDivisions.setLayoutManager(new GridLayoutManager(this, spanCount));
        binding.rvDivisions.setAdapter(adapter);
    }

    // ── Topic 10: Broadcast Receiver Setup ───────────────────────────────────

    private void setupNetworkReceiver() {
        networkReceiver = new NetworkChangeReceiver(this);
        // Initial state check
        onNetworkChanged(NetworkChangeReceiver.isNetworkAvailable(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register BroadcastReceiver dynamically for connectivity changes
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (networkReceiver != null) {
            try {
                unregisterReceiver(networkReceiver);
            } catch (Exception ignored) {}
        }
    }

    private Boolean previousNetworkState = null;

    @Override
    public void onNetworkChanged(boolean isConnected) {
        binding.layoutOfflineBanner.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        
        if (previousNetworkState != null && previousNetworkState != isConnected) {
            if (isConnected) {
                Toast.makeText(this, "🌐 Internet connection restored (Online Mode)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Internet disconnected (Offline Mode)", Toast.LENGTH_SHORT).show();
            }
        }
        previousNetworkState = isConnected;
    }

    // ── Topic 11: HTTP Network Request, Background Operation & JSON Parsing ──

    private void fetchLiveWeather() {
        // Sylhet coordinates: 24.8949° N, 91.8687° E
        WeatherApiClient.getInstance().fetchWeather(24.8949, 91.8687, new WeatherApiClient.WeatherCallback() {
            @Override
            public void onSuccess(WeatherApiClient.WeatherInfo info) {
                binding.tvWeatherTemp.setText(String.format(
                        "%s সিলেট: %.1f°C (%s)", info.emoji, info.temperature, info.conditionBn));
            }

            @Override
            public void onError(String errorMessage) {
                binding.tvWeatherTemp.setText("⛅ সিলেট: ২৬°C (অনুকূল আবহাওয়া)");
            }
        });
    }

    // ── Navigation & SQLite / Firebase Handling ──────────────────────────────

    @Override
    public void onDivisionClick(Division division) {
        Intent intent = new Intent(this, DistrictListActivity.class);
        intent.putExtra(DistrictListActivity.EXTRA_DIVISION_ID, division.getId());
        intent.putExtra(DistrictListActivity.EXTRA_DIVISION_NAME, division.getName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Already home
        } else if (id == R.id.nav_profile) {
            if (FirebaseAuthManager.getInstance().isLoggedIn()) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } else if (id == R.id.nav_cloud_sync) {
            triggerCloudSync();
        } else if (id == R.id.nav_theme_toggle) {
            handleThemeToggle();
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_auth) {
            if (FirebaseAuthManager.getInstance().isLoggedIn()) {
                showLogoutDialog();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    private void triggerCloudSync() {
        if (!NetworkChangeReceiver.isNetworkAvailable(this)) {
            Toast.makeText(this, "⚠️ You are offline. Data is saved locally. Cloud sync will happen when online.", Toast.LENGTH_LONG).show();
            return;
        }
        
        Toast.makeText(this, "Firebase Cloud Database is syncing...", Toast.LENGTH_SHORT).show();
        FirebaseManager.getInstance().syncFavoritesWithCloud(this, new FirebaseManager.CloudSyncCallback() {
            @Override
            public void onSyncSuccess(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSyncFailure(String error) {
                Toast.makeText(MainActivity.this, "Sync failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

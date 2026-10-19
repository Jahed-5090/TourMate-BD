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
import com.tourmate.bd.database.TourMateDatabaseHelper;
import com.tourmate.bd.databinding.ActivityMainBinding;
import com.tourmate.bd.models.Division;
import com.tourmate.bd.models.TouristSpot;
import com.tourmate.bd.network.FirebaseManager;
import com.tourmate.bd.network.WeatherApiClient;

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

        setupToolbar();
        setupDrawer();
        setupDivisionGrid();
        setupNetworkReceiver();
        fetchLiveWeather();
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
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void setupDivisionGrid() {
        List<Division> divisions = DataRepository.getInstance().getAllDivisions();
        adapter = new DivisionAdapter(this, divisions, this);
        binding.rvDivisions.setLayoutManager(new GridLayoutManager(this, 2));
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

    @Override
    public void onNetworkChanged(boolean isConnected) {
        binding.layoutOfflineBanner.setVisibility(isConnected ? View.GONE : View.VISIBLE);
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
        } else if (id == R.id.nav_favorites) {
            showFavoritesDialog();
        } else if (id == R.id.nav_cloud_sync) {
            triggerCloudSync();
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFavoritesDialog() {
        TourMateDatabaseHelper db = TourMateDatabaseHelper.getInstance(this);
        List<String> favIds = db.getAllFavoriteSpotIds();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❤️ সংরক্ষিত স্থানসমূহ (" + favIds.size() + ")");

        if (favIds.isEmpty()) {
            builder.setMessage("আপনার কোনো পছন্দের স্থান সংরক্ষিত নেই। যেকোনো স্পটের বিস্তারিত পেজে গিয়ে বুকমার্ক আইকনে ট্যাপ করে সংরক্ষণ করুন।");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String spotId : favIds) {
                TouristSpot spot = DataRepository.getInstance().getSpotById(spotId);
                if (spot != null) {
                    sb.append("• ").append(spot.getName()).append(" (").append(spot.getNameBn()).append(")\n");
                }
            }
            builder.setMessage(sb.toString().trim());
        }

        builder.setPositiveButton("ঠিক আছে", null);
        builder.show();
    }

    private void triggerCloudSync() {
        Toast.makeText(this, "Firebase ক্লাউড ডাটাবেজের সাথে সিঙ্ক হচ্ছে...", Toast.LENGTH_SHORT).show();
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

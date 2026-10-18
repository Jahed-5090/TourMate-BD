package com.tourmate.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.tourmate.bd.R;
import com.tourmate.bd.adapters.DivisionAdapter;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivityMainBinding;
import com.tourmate.bd.models.Division;

import java.util.List;

/**
 * Home screen displaying all Divisions of Bangladesh as a grid.
 * Features a navigation drawer for app sections.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DivisionAdapter.OnDivisionClickListener {

    private ActivityMainBinding binding;
    private DivisionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupDrawer();
        setupDivisionGrid();
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
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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

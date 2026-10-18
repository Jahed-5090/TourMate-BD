package com.tourmate.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tourmate.bd.R;
import com.tourmate.bd.adapters.SpotAdapter;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivitySpotListBinding;
import com.tourmate.bd.models.TouristSpot;

import java.util.List;

/**
 * Displays list of Tourist Spots within a selected District.
 * Features a search bar and category filter chips.
 */
public class SpotListActivity extends AppCompatActivity
        implements SpotAdapter.OnSpotClickListener {

    public static final String EXTRA_DISTRICT_ID = "extra_district_id";
    public static final String EXTRA_DISTRICT_NAME = "extra_district_name";

    private ActivitySpotListBinding binding;
    private SpotAdapter adapter;
    private List<TouristSpot> allSpots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpotListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String districtId = getIntent().getStringExtra(EXTRA_DISTRICT_ID);
        String districtName = getIntent().getStringExtra(EXTRA_DISTRICT_NAME);

        setupToolbar(districtName);
        setupSpotList(districtId);
    }

    private void setupToolbar(String districtName) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(districtName + " " + getString(R.string.district_label));
        }
    }

    private void setupSpotList(String districtId) {
        allSpots = DataRepository.getInstance().getSpotsByDistrict(districtId);

        if (allSpots.isEmpty()) {
            binding.tvEmptySpots.setVisibility(View.VISIBLE);
            binding.rvSpots.setVisibility(View.GONE);
        } else {
            binding.tvEmptySpots.setVisibility(View.GONE);
            adapter = new SpotAdapter(this, allSpots, this);
            binding.rvSpots.setLayoutManager(new LinearLayoutManager(this));
            binding.rvSpots.setAdapter(adapter);
        }
    }

    @Override
    public void onSpotClick(TouristSpot spot) {
        Intent intent = new Intent(this, SpotDetailActivity.class);
        intent.putExtra(SpotDetailActivity.EXTRA_SPOT_ID, spot.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
}

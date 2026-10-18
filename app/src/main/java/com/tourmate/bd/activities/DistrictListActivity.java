package com.tourmate.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tourmate.bd.R;
import com.tourmate.bd.adapters.DistrictAdapter;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivityDistrictListBinding;
import com.tourmate.bd.models.District;

import java.util.List;

/**
 * Shows list of Districts within a selected Division.
 */
public class DistrictListActivity extends AppCompatActivity
        implements DistrictAdapter.OnDistrictClickListener {

    public static final String EXTRA_DIVISION_ID = "extra_division_id";
    public static final String EXTRA_DIVISION_NAME = "extra_division_name";

    private ActivityDistrictListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDistrictListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String divisionId = getIntent().getStringExtra(EXTRA_DIVISION_ID);
        String divisionName = getIntent().getStringExtra(EXTRA_DIVISION_NAME);

        setupToolbar(divisionName);
        setupDistrictList(divisionId);
    }

    private void setupToolbar(String divisionName) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(divisionName + " " + getString(R.string.division_label));
        }
    }

    private void setupDistrictList(String divisionId) {
        List<District> districts = DataRepository.getInstance().getDistrictsByDivision(divisionId);

        if (districts.isEmpty()) {
            binding.tvComingSoon.setVisibility(android.view.View.VISIBLE);
            binding.rvDistricts.setVisibility(android.view.View.GONE);
        } else {
            binding.tvComingSoon.setVisibility(android.view.View.GONE);
            DistrictAdapter adapter = new DistrictAdapter(this, districts, this);
            binding.rvDistricts.setLayoutManager(new LinearLayoutManager(this));
            binding.rvDistricts.setAdapter(adapter);
        }
    }

    @Override
    public void onDistrictClick(District district) {
        Intent intent = new Intent(this, SpotListActivity.class);
        intent.putExtra(SpotListActivity.EXTRA_DISTRICT_ID, district.getId());
        intent.putExtra(SpotListActivity.EXTRA_DISTRICT_NAME, district.getName());
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

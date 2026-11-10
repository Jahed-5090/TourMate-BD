package com.tourmate.bd.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourmate.bd.R;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivityAddSpotBinding;
import com.tourmate.bd.models.District;
import com.tourmate.bd.models.Division;
import com.tourmate.bd.models.TouristSpot;

import java.util.ArrayList;
import java.util.List;

public class AddSpotActivity extends AppCompatActivity {

    private ActivityAddSpotBinding binding;
    private Uri imageUri;
    
    private List<Division> divisions;
    private List<District> districts;
    private String selectedDistrictId;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(imageUri).into(binding.ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddSpotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_primary_dark));
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupSpinners();

        binding.btnSelectImage.setOnClickListener(v -> getContent.launch("image/*"));
        binding.btnSaveSpot.setOnClickListener(v -> saveSpot());
    }

    private void setupSpinners() {
        divisions = DataRepository.getInstance().getAllDivisions();
        List<String> divNames = new ArrayList<>();
        for (Division d : divisions) divNames.add(d.getName());

        ArrayAdapter<String> divAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divNames);
        divAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDivision.setAdapter(divAdapter);

        binding.spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Division selectedDiv = divisions.get(position);
                updateDistrictSpinner(selectedDiv.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDistrictSpinner(String divisionId) {
        districts = DataRepository.getInstance().getDistrictsByDivision(divisionId);
        List<String> distNames = new ArrayList<>();
        for (District d : districts) distNames.add(d.getName());

        ArrayAdapter<String> distAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distNames);
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(distAdapter);

        binding.spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDistrictId = districts.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveSpot() {
        String nameEn = binding.etNameEn.getText().toString().trim();
        String nameBn = binding.etNameBn.getText().toString().trim();
        String shortDesc = binding.etShortDesc.getText().toString().trim();
        String fullDesc = binding.etFullDesc.getText().toString().trim();

        if (nameEn.isEmpty() || nameBn.isEmpty() || shortDesc.isEmpty() || fullDesc.isEmpty()) {
            Toast.makeText(this, "Please fill in all text fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDistrictId == null) {
            Toast.makeText(this, "Please select a district", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveSpot.setEnabled(false);

        String spotId = "dyn_" + System.currentTimeMillis();

        if (imageUri != null) {
            new Thread(() -> {
                try {
                    android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    // Scale bitmap down to max 800x800 to save Firestore space
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    float ratioBitmap = (float) width / (float) height;
                    int bound = 800;
                    int finalWidth = bound;
                    int finalHeight = bound;
                    if (ratioBitmap > 1) {
                        finalHeight = (int) (finalWidth / ratioBitmap);
                    } else {
                        finalWidth = (int) (finalHeight * ratioBitmap);
                    }
                    android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
                    
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] data = baos.toByteArray();
                    Blob blob = Blob.fromBytes(data);
                    
                    java.util.Map<String, Object> docData = new java.util.HashMap<>();
                    docData.put("image_blob", blob);
                    
                    FirebaseFirestore.getInstance().collection("spot_images").document(spotId)
                            .set(docData)
                            .addOnSuccessListener(aVoid -> runOnUiThread(() -> uploadData(spotId, nameEn, nameBn, shortDesc, fullDesc, "firestore:" + spotId)))
                            .addOnFailureListener(err -> runOnUiThread(() -> {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.btnSaveSpot.setEnabled(true);
                                Toast.makeText(AddSpotActivity.this, "Image upload failed: " + err.getMessage(), Toast.LENGTH_LONG).show();
                            }));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSaveSpot.setEnabled(true);
                        Toast.makeText(AddSpotActivity.this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        } else {
            uploadData(spotId, nameEn, nameBn, shortDesc, fullDesc, "");
        }
    }

    private void uploadData(String spotId, String nameEn, String nameBn, String shortDesc, String fullDesc, String imageUrl) {
        TouristSpot spot = new TouristSpot();
        spot.setId(spotId);
        spot.setDistrictId(selectedDistrictId);
        spot.setName(nameEn);
        spot.setNameBn(nameBn);
        spot.setShortDescription(shortDesc);
        spot.setFullDescription(fullDesc);
        spot.setImageUrl(imageUrl);
        spot.setTimestamp(System.currentTimeMillis());

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("dynamic_spots").child(spotId);
        dbRef.setValue(spot)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Spot added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveSpot.setEnabled(true);
                    Toast.makeText(this, "Failed to save spot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

package com.tourmate.bd.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourmate.bd.R;
import com.tourmate.bd.adapters.SpotPhotoAdapter;
import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.databinding.ActivityEditSpotBinding;
import com.tourmate.bd.models.TouristSpot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSpotActivity extends AppCompatActivity {

    public static final String EXTRA_SPOT_ID = "extra_spot_id";

    private ActivityEditSpotBinding binding;
    private TouristSpot spot;

    private Uri newHeaderUri;
    private final List<Uri> newGalleryUris = new ArrayList<>();
    private final List<String> galleryImageUrls = new ArrayList<>();
    private final List<SpotPhotoAdapter.PhotoItem> editGalleryItems = new ArrayList<>();
    private SpotPhotoAdapter galleryAdapter;

    private final ActivityResultLauncher<String> getHeaderImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newHeaderUri = uri;
                    Glide.with(this).load(newHeaderUri).into(binding.ivHeaderPreview);
                }
            });

    private final ActivityResultLauncher<String> getGalleryImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newGalleryUris.add(uri);
                    editGalleryItems.add(new SpotPhotoAdapter.PhotoItem(uri));
                    if (galleryAdapter != null) {
                        galleryAdapter.notifyDataSetChanged();
                    }
                    updateGalleryCount();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditSpotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.brand_primary_dark));

        String spotId = getIntent().getStringExtra(EXTRA_SPOT_ID);
        if (spotId != null) {
            spot = DataRepository.getInstance().getSpotById(spotId);
        }

        if (spot == null) {
            Toast.makeText(this, "Spot not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        populateExistingData();

        binding.btnChangeHeaderImage.setOnClickListener(v -> getHeaderImage.launch("image/*"));
        binding.btnAddGalleryImage.setOnClickListener(v -> getGalleryImage.launch("image/*"));
        binding.btnSaveChanges.setOnClickListener(v -> saveAllChanges());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit " + spot.getName());
        }
    }

    private void populateExistingData() {
        // Main header image
        if (spot.getImageUrl() != null && !spot.getImageUrl().isEmpty()) {
            if (spot.getImageUrl().startsWith("firestore:")) {
                String docId = spot.getImageUrl().replace("firestore:", "");
                FirebaseFirestore.getInstance().collection("spot_images").document(docId).get()
                        .addOnSuccessListener(document -> {
                            Blob imageBlob = document.getBlob("image_blob");
                            if (imageBlob != null) {
                                byte[] bytes = imageBlob.toBytes();
                                Glide.with(this).load(bytes).into(binding.ivHeaderPreview);
                            }
                        });
            } else {
                Glide.with(this).load(spot.getImageUrl()).into(binding.ivHeaderPreview);
            }
        } else if (spot.getImageResId() != 0) {
            binding.ivHeaderPreview.setImageResource(spot.getImageResId());
        }

        // Populate existing photobooth / gallery photos (resource IDs + dynamic URLs)
        editGalleryItems.clear();
        if (spot.getGalleryImages() != null) {
            for (Integer resId : spot.getGalleryImages()) {
                if (resId != null && resId != 0) {
                    editGalleryItems.add(new SpotPhotoAdapter.PhotoItem(resId));
                }
            }
        }
        if (spot.getGalleryImageUrls() != null) {
            galleryImageUrls.addAll(spot.getGalleryImageUrls());
            for (String url : spot.getGalleryImageUrls()) {
                if (url != null && !url.isEmpty()) {
                    editGalleryItems.add(new SpotPhotoAdapter.PhotoItem(url));
                }
            }
        }

        binding.rvEditGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        galleryAdapter = new SpotPhotoAdapter(this, editGalleryItems, (item, position) -> {
            // Optional: notify or click preview
        }, true);
        binding.rvEditGallery.setAdapter(galleryAdapter);

        updateGalleryCount();

        // Fields
        setTextIfNotNull(binding.etNameEn, spot.getName());
        setTextIfNotNull(binding.etNameBn, spot.getNameBn());
        setTextIfNotNull(binding.etShortDesc, spot.getShortDescription());
        setTextIfNotNull(binding.etFullDesc, spot.getFullDescription());
        setTextIfNotNull(binding.etHowToReach, spot.getHowToReach());
        setTextIfNotNull(binding.etTransport, spot.getTransportOptions());
        setTextIfNotNull(binding.etCostSylhet, spot.getApproxCostFromSylhet());
        setTextIfNotNull(binding.etEntranceFee, spot.getEntranceFee());
        setTextIfNotNull(binding.etBestTime, spot.getBestTimeToVisit());
        setTextIfNotNull(binding.etTravelTips, spot.getTravelTips());
        setTextIfNotNull(binding.etWarnings, spot.getWarnings());
        setTextIfNotNull(binding.etYoutubeId, spot.getYoutubeVideoId());
    }

    private void setTextIfNotNull(android.widget.EditText editText, String text) {
        if (text != null) {
            editText.setText(text);
        }
    }

    private void updateGalleryCount() {
        int total = editGalleryItems.size();
        binding.tvGalleryCount.setText(total + " Photobooth Photo" + (total != 1 ? "s" : ""));
    }

    private void saveAllChanges() {
        String nameEn = binding.etNameEn.getText().toString().trim();
        String nameBn = binding.etNameBn.getText().toString().trim();
        String shortDesc = binding.etShortDesc.getText().toString().trim();
        String fullDesc = binding.etFullDesc.getText().toString().trim();

        if (nameEn.isEmpty() || nameBn.isEmpty() || shortDesc.isEmpty() || fullDesc.isEmpty()) {
            Toast.makeText(this, "Please fill in all basic text fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveChanges.setEnabled(false);

        new Thread(() -> {
            try {
                // 1. Process header image if newly selected
                if (newHeaderUri != null) {
                    byte[] headerBytes = compressImageUri(newHeaderUri);
                    if (headerBytes != null) {
                        Blob blob = Blob.fromBytes(headerBytes);
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("image_blob", blob);
                        FirebaseFirestore.getInstance().collection("spot_images").document(spot.getId()).set(docData);
                        spot.setImageUrl("firestore:" + spot.getId());
                    }
                }

                // 2. Process new gallery images
                for (Uri galUri : newGalleryUris) {
                    byte[] galBytes = compressImageUri(galUri);
                    if (galBytes != null) {
                        String galDocId = "gal_" + spot.getId() + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
                        Blob blob = Blob.fromBytes(galBytes);
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("image_blob", blob);
                        FirebaseFirestore.getInstance().collection("spot_images").document(galDocId).set(docData);
                        galleryImageUrls.add("firestore:" + galDocId);
                    }
                }

                // 3. Update model fields
                spot.setName(nameEn);
                spot.setNameBn(nameBn);
                spot.setShortDescription(shortDesc);
                spot.setFullDescription(fullDesc);
                spot.setHowToReach(binding.etHowToReach.getText().toString().trim());
                spot.setTransportOptions(binding.etTransport.getText().toString().trim());
                spot.setApproxCostFromSylhet(binding.etCostSylhet.getText().toString().trim());
                spot.setEntranceFee(binding.etEntranceFee.getText().toString().trim());
                spot.setBestTimeToVisit(binding.etBestTime.getText().toString().trim());
                spot.setTravelTips(binding.etTravelTips.getText().toString().trim());
                spot.setWarnings(binding.etWarnings.getText().toString().trim());
                spot.setYoutubeVideoId(binding.etYoutubeId.getText().toString().trim());
                spot.setGalleryImageUrls(galleryImageUrls);
                spot.setTimestamp(System.currentTimeMillis());

                // 4. Save to Firebase Realtime Database
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("dynamic_spots").child(spot.getId());
                dbRef.setValue(spot).addOnCompleteListener(task -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSaveChanges.setEnabled(true);
                        if (task.isSuccessful()) {
                            DataRepository.getInstance().updateSpot(spot);
                            Toast.makeText(EditSpotActivity.this, "Spot updated successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditSpotActivity.this, "Database update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveChanges.setEnabled(true);
                    Toast.makeText(EditSpotActivity.this, "Error saving spot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private byte[] compressImageUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap == null) return null;

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
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
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

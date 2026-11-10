package com.tourmate.bd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourmate.bd.R;

import java.util.List;

/**
 * RecyclerView Adapter for ViewPager2 full-screen swipeable photo gallery.
 * Supports static drawable resources, dynamic Firestore Blobs, and local Uris.
 */
public class FullScreenImageAdapter extends RecyclerView.Adapter<FullScreenImageAdapter.ViewHolder> {

    private final Context context;
    private final List<SpotPhotoAdapter.PhotoItem> items;

    public FullScreenImageAdapter(Context context, List<SpotPhotoAdapter.PhotoItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_full_screen_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpotPhotoAdapter.PhotoItem item = items.get(position);

        if (item.uri != null) {
            Glide.with(context)
                    .load(item.uri)
                    .placeholder(R.drawable.placeholder_spot)
                    .into(holder.imageView);
        } else if (item.url != null && !item.url.isEmpty()) {
            if (item.url.startsWith("firestore:")) {
                String docId = item.url.replace("firestore:", "");
                FirebaseFirestore.getInstance().collection("spot_images").document(docId).get()
                        .addOnSuccessListener(document -> {
                            Blob imageBlob = document.getBlob("image_blob");
                            if (imageBlob != null) {
                                byte[] imageByteArray = imageBlob.toBytes();
                                Glide.with(context)
                                        .load(imageByteArray)
                                        .placeholder(R.drawable.placeholder_spot)
                                        .into(holder.imageView);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(item.url)
                        .placeholder(R.drawable.placeholder_spot)
                        .into(holder.imageView);
            }
        } else if (item.resId != null && item.resId != 0) {
            holder.imageView.setImageResource(item.resId);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_spot);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_full_screen);
        }
    }
}

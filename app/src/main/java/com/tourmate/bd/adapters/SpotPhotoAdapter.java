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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the horizontal photo gallery strip in SpotDetailActivity.
 * Displays a scrollable row of spot photos sourced from drawable resources or dynamic URLs/Firestore.
 */
public class SpotPhotoAdapter extends RecyclerView.Adapter<SpotPhotoAdapter.PhotoViewHolder> {

    public static class PhotoItem {
        public final Integer resId;
        public final String url;
        public final android.net.Uri uri;

        public PhotoItem(Integer resId) {
            this.resId = resId;
            this.url = null;
            this.uri = null;
        }

        public PhotoItem(String url) {
            this.resId = null;
            this.url = url;
            this.uri = null;
        }

        public PhotoItem(android.net.Uri uri) {
            this.resId = null;
            this.url = null;
            this.uri = uri;
        }
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoItem item, int position);
    }

    private final Context context;
    private final List<PhotoItem> items;
    private final OnPhotoClickListener listener;

    public SpotPhotoAdapter(Context context, List<Integer> photoResIds, OnPhotoClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        if (photoResIds != null) {
            for (Integer id : photoResIds) {
                this.items.add(new PhotoItem(id));
            }
        }
        this.listener = listener;
    }

    public SpotPhotoAdapter(Context context, List<PhotoItem> items, OnPhotoClickListener listener, boolean isPhotoItems) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoItem item = items.get(position);

        if (item.uri != null) {
            Glide.with(context)
                    .load(item.uri)
                    .placeholder(R.drawable.placeholder_spot)
                    .into(holder.ivPhoto);
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
                                        .into(holder.ivPhoto);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(item.url)
                        .placeholder(R.drawable.placeholder_spot)
                        .into(holder.ivPhoto);
            }
        } else if (item.resId != null && item.resId != 0) {
            holder.ivPhoto.setImageResource(item.resId);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.placeholder_spot);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_gallery_photo);
        }
    }
}

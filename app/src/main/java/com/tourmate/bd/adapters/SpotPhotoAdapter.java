package com.tourmate.bd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tourmate.bd.R;

import java.util.List;

/**
 * Adapter for the horizontal photo gallery strip in SpotDetailActivity.
 * Displays a scrollable row of spot photos sourced from drawable resources.
 */
public class SpotPhotoAdapter extends RecyclerView.Adapter<SpotPhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(int resId, int position);
    }

    private final Context context;
    private final List<Integer> photoResIds;
    private final OnPhotoClickListener listener;

    public SpotPhotoAdapter(Context context, List<Integer> photoResIds, OnPhotoClickListener listener) {
        this.context = context;
        this.photoResIds = photoResIds;
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
        int resId = photoResIds.get(position);
        holder.ivPhoto.setImageResource(resId);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(resId, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoResIds != null ? photoResIds.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_gallery_photo);
        }
    }
}

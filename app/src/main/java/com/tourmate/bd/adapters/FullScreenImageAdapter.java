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
 * RecyclerView Adapter for ViewPager2 full-screen swipeable photo gallery.
 */
public class FullScreenImageAdapter extends RecyclerView.Adapter<FullScreenImageAdapter.ViewHolder> {

    private final Context context;
    private final List<Integer> imageResIds;

    public FullScreenImageAdapter(Context context, List<Integer> imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_full_screen_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int resId = imageResIds.get(position);
        holder.imageView.setImageResource(resId);
    }

    @Override
    public int getItemCount() {
        return imageResIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_full_screen);
        }
    }
}

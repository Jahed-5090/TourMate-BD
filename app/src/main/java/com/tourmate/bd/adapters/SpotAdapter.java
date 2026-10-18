package com.tourmate.bd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tourmate.bd.R;
import com.tourmate.bd.models.TouristSpot;

import java.util.List;

/**
 * RecyclerView Adapter for displaying Tourist Spots in a list.
 * Each item shows spot image, name, category badge, and short description.
 */
public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {

    public interface OnSpotClickListener {
        void onSpotClick(TouristSpot spot);
    }

    private final Context context;
    private final List<TouristSpot> spots;
    private final OnSpotClickListener listener;

    public SpotAdapter(Context context, List<TouristSpot> spots, OnSpotClickListener listener) {
        this.context = context;
        this.spots = spots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        TouristSpot spot = spots.get(position);
        holder.bind(spot, position + 1);
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    class SpotViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSpot;
        TextView tvNumber;
        TextView tvName;
        TextView tvNameBn;
        TextView tvCategory;
        TextView tvShortDesc;

        SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSpot = itemView.findViewById(R.id.iv_spot);
            tvNumber = itemView.findViewById(R.id.tv_spot_number);
            tvName = itemView.findViewById(R.id.tv_spot_name);
            tvNameBn = itemView.findViewById(R.id.tv_spot_name_bn);
            tvCategory = itemView.findViewById(R.id.tv_spot_category);
            tvShortDesc = itemView.findViewById(R.id.tv_spot_short_desc);
        }

        void bind(TouristSpot spot, int number) {
            ivSpot.setImageResource(spot.getImageResId());
            tvNumber.setText(String.valueOf(number));
            tvName.setText(spot.getName());
            tvNameBn.setText(spot.getNameBn());

            if (spot.getCategory() != null) {
                tvCategory.setText(spot.getCategory().getDisplayName());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            if (spot.getShortDescription() != null) {
                tvShortDesc.setText(spot.getShortDescription());
                tvShortDesc.setVisibility(View.VISIBLE);
            } else {
                tvShortDesc.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onSpotClick(spot));
        }
    }
}

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
import com.tourmate.bd.models.District;

import java.util.List;

/**
 * RecyclerView Adapter for displaying Districts in a list.
 * Each row shows district image, name, and description.
 */
public class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.DistrictViewHolder> {

    public interface OnDistrictClickListener {
        void onDistrictClick(District district);
    }

    private final Context context;
    private final List<District> districts;
    private final OnDistrictClickListener listener;

    public DistrictAdapter(Context context, List<District> districts, OnDistrictClickListener listener) {
        this.context = context;
        this.districts = districts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DistrictViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_district, parent, false);
        return new DistrictViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DistrictViewHolder holder, int position) {
        District district = districts.get(position);
        holder.bind(district, position + 1);
    }

    @Override
    public int getItemCount() {
        return districts.size();
    }

    class DistrictViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDistrict;
        TextView tvNumber;
        TextView tvName;
        TextView tvNameBn;
        TextView tvDescription;

        DistrictViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDistrict = itemView.findViewById(R.id.iv_district);
            tvNumber = itemView.findViewById(R.id.tv_district_number);
            tvName = itemView.findViewById(R.id.tv_district_name);
            tvNameBn = itemView.findViewById(R.id.tv_district_name_bn);
            tvDescription = itemView.findViewById(R.id.tv_district_description);
        }

        void bind(District district, int number) {
            ivDistrict.setImageResource(district.getImageResId());
            tvNumber.setText(String.valueOf(number));
            tvName.setText(district.getName());
            tvNameBn.setText(district.getNameBn());
            if (district.getDescription() != null) {
                tvDescription.setText(district.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onDistrictClick(district));
        }
    }
}

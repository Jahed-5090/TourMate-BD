package com.tourmate.bd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tourmate.bd.R;
import com.tourmate.bd.models.Division;

import java.util.List;

/**
 * RecyclerView Adapter for displaying Divisions in a 2-column grid.
 * Each card shows the division image, name (Bangla), and an overlay gradient.
 */
public class DivisionAdapter extends RecyclerView.Adapter<DivisionAdapter.DivisionViewHolder> {

    public interface OnDivisionClickListener {
        void onDivisionClick(Division division);
    }

    private final Context context;
    private final List<Division> divisions;
    private final OnDivisionClickListener listener;

    public DivisionAdapter(Context context, List<Division> divisions, OnDivisionClickListener listener) {
        this.context = context;
        this.divisions = divisions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DivisionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_division, parent, false);
        return new DivisionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DivisionViewHolder holder, int position) {
        Division division = divisions.get(position);
        holder.bind(division);
    }

    @Override
    public int getItemCount() {
        return divisions.size();
    }

    class DivisionViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivDivision;
        TextView tvNameBn;
        TextView tvName;

        DivisionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_division);
            ivDivision = itemView.findViewById(R.id.iv_division);
            tvNameBn = itemView.findViewById(R.id.tv_division_name_bn);
            tvName = itemView.findViewById(R.id.tv_division_name);
        }

        void bind(Division division) {
            ivDivision.setImageResource(division.getImageResId());
            tvNameBn.setText(division.getNameBn());
            tvName.setText(division.getName());

            cardView.setOnClickListener(v -> {
                // Scale animation on click
                cardView.animate()
                        .scaleX(0.95f).scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            cardView.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .withEndAction(() -> listener.onDivisionClick(division))
                                    .start();
                        })
                        .start();
            });
        }
    }
}

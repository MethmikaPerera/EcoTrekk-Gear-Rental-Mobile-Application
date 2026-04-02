package com.akigen.ecotrekk.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.HomeFeaturedGear;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFeaturedGearAdapter extends RecyclerView.Adapter<HomeFeaturedGearAdapter.FeaturedGearViewHolder> {

    public interface OnFeaturedGearClickListener {
        void onFeaturedGearClick(HomeFeaturedGear gear);
    }

    private final List<HomeFeaturedGear> featuredGears = new ArrayList<>();
    private final OnFeaturedGearClickListener onFeaturedGearClickListener;

    public HomeFeaturedGearAdapter() {
        this(null);
    }

    public HomeFeaturedGearAdapter(OnFeaturedGearClickListener onFeaturedGearClickListener) {
        this.onFeaturedGearClickListener = onFeaturedGearClickListener;
    }

    public void submitList(List<HomeFeaturedGear> data) {
        featuredGears.clear();
        featuredGears.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedGearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_featured_gear, parent, false);
        return new FeaturedGearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedGearViewHolder holder, int position) {
        HomeFeaturedGear gear = featuredGears.get(position);
        if (!TextUtils.isEmpty(gear.getImageUrl())) {
            Glide.with(holder.imgFeaturedGear)
                    .load(gear.getImageUrl())
                    .placeholder(gear.getImageResId())
                    .error(gear.getImageResId())
                    .centerCrop()
                    .into(holder.imgFeaturedGear);
        } else {
            holder.imgFeaturedGear.setImageResource(gear.getImageResId());
        }
        holder.txtFeaturedGearName.setText(gear.getName());
        holder.txtFeaturedGearCategory.setText(gear.getCategory());
        holder.txtFeaturedGearPrice.setText(String.format(Locale.US, "LKR %,.2f / day", gear.getPerDayPrice()));
        holder.itemView.setOnClickListener(v -> {
            if (onFeaturedGearClickListener != null) {
                onFeaturedGearClickListener.onFeaturedGearClick(gear);
            }
        });
    }

    @Override
    public int getItemCount() {
        return featuredGears.size();
    }

    static class FeaturedGearViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgFeaturedGear;
        private final TextView txtFeaturedGearName;
        private final TextView txtFeaturedGearCategory;
        private final TextView txtFeaturedGearPrice;

        FeaturedGearViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFeaturedGear = itemView.findViewById(R.id.imgFeaturedGear);
            txtFeaturedGearName = itemView.findViewById(R.id.txtFeaturedGearName);
            txtFeaturedGearCategory = itemView.findViewById(R.id.txtFeaturedGearCategory);
            txtFeaturedGearPrice = itemView.findViewById(R.id.txtFeaturedGearPrice);
        }
    }
}

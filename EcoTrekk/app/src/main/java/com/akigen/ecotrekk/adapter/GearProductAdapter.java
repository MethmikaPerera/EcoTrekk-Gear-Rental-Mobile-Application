package com.akigen.ecotrekk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.GearProduct;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GearProductAdapter extends RecyclerView.Adapter<GearProductAdapter.GearViewHolder> {

    public interface OnGearClickListener {
        void onGearClick(GearProduct product);
    }

    private final List<GearProduct> gearProducts = new ArrayList<>();
    private final OnGearClickListener onGearClickListener;

    public GearProductAdapter(OnGearClickListener onGearClickListener) {
        this.onGearClickListener = onGearClickListener;
    }

    public void submitList(List<GearProduct> products) {
        gearProducts.clear();
        gearProducts.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gear_product, parent, false);
        return new GearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GearViewHolder holder, int position) {
        GearProduct product = gearProducts.get(position);
        holder.txtGearName.setText(product.getName());
        holder.txtGearCategory.setText(product.getCategory());
        holder.txtGearPrice.setText(String.format(Locale.US, "LKR %,.2f / day", product.getPerDayPrice()));
        Glide.with(holder.imgGear)
                .load(product.getImageUrl())
                .placeholder(product.getImageResId())
                .error(product.getImageResId())
                .centerCrop()
                .into(holder.imgGear);
        holder.itemView.setOnClickListener(v -> {
            if (onGearClickListener != null) {
                onGearClickListener.onGearClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gearProducts.size();
    }

    class GearViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgGear;
        private final TextView txtGearName;
        private final TextView txtGearCategory;
        private final TextView txtGearPrice;

        GearViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGear = itemView.findViewById(R.id.imgGear);
            txtGearName = itemView.findViewById(R.id.txtGearName);
            txtGearCategory = itemView.findViewById(R.id.txtGearCategory);
            txtGearPrice = itemView.findViewById(R.id.txtGearPrice);
        }
    }
}

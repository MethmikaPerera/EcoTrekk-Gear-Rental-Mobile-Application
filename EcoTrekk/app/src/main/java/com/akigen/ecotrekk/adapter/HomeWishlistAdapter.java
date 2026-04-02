package com.akigen.ecotrekk.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.HomeFeaturedGear;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeWishlistAdapter extends RecyclerView.Adapter<HomeWishlistAdapter.WishlistViewHolder> {

    public interface OnWishlistItemClickListener {
        void onWishlistItemClick(HomeFeaturedGear gear);
    }

    private final List<HomeFeaturedGear> wishlistItems = new ArrayList<>();
    private final OnWishlistItemClickListener listener;

    public HomeWishlistAdapter(OnWishlistItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<HomeFeaturedGear> data) {
        wishlistItems.clear();
        wishlistItems.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_wishlist_gear, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        HomeFeaturedGear item = wishlistItems.get(position);

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(holder.imgWishlistGear)
                    .load(item.getImageUrl())
                    .placeholder(item.getImageResId())
                    .error(item.getImageResId())
                    .centerCrop()
                    .into(holder.imgWishlistGear);
        } else {
            holder.imgWishlistGear.setImageResource(item.getImageResId());
        }

        holder.txtWishlistName.setText(item.getName());
        holder.txtWishlistCategory.setText(item.getCategory());
        holder.txtWishlistPrice.setText(String.format(Locale.US, "LKR %,.2f / day", item.getPerDayPrice()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWishlistItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imgWishlistGear;
        private final TextView txtWishlistName;
        private final TextView txtWishlistCategory;
        private final TextView txtWishlistPrice;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWishlistGear = itemView.findViewById(R.id.imgWishlistGear);
            txtWishlistName = itemView.findViewById(R.id.txtWishlistName);
            txtWishlistCategory = itemView.findViewById(R.id.txtWishlistCategory);
            txtWishlistPrice = itemView.findViewById(R.id.txtWishlistPrice);
        }
    }
}

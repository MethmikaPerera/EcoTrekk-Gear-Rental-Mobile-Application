package com.akigen.ecotrekk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.model.HomeCategory;
import com.bumptech.glide.Glide;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(HomeCategory category);
    }

    private final List<HomeCategory> categories = new ArrayList<>();
    private final OnCategoryClickListener onCategoryClickListener;

    public HomeCategoryAdapter() {
        this(null);
    }

    public HomeCategoryAdapter(OnCategoryClickListener onCategoryClickListener) {
        this.onCategoryClickListener = onCategoryClickListener;
    }

    public void submitList(List<HomeCategory> data) {
        categories.clear();
        categories.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        HomeCategory category = categories.get(position);
        if (!TextUtils.isEmpty(category.getImageUrl())) {
            Glide.with(holder.imgCategory)
                    .load(category.getImageUrl())
                    .placeholder(category.getImageResId())
                    .error(category.getImageResId())
                    .centerCrop()
                    .into(holder.imgCategory);
        } else {
            holder.imgCategory.setImageResource(category.getImageResId());
        }
        holder.txtCategoryName.setText(category.getName());
        holder.txtCategoryCount.setText(category.getItemCountLabel());
        holder.itemView.setOnClickListener(v -> {
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCategory;
        private final TextView txtCategoryName;
        private final TextView txtCategoryCount;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            txtCategoryCount = itemView.findViewById(R.id.txtCategoryCount);
        }
    }
}

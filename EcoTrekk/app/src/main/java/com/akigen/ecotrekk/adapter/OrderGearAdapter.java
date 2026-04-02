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
import com.akigen.ecotrekk.dto.OrderItemResponse;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class OrderGearAdapter extends RecyclerView.Adapter<OrderGearAdapter.OrderGearViewHolder> {

    private final List<OrderItemResponse> items = new ArrayList<>();

    public void submitList(List<OrderItemResponse> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderGearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_gear, parent, false);
        return new OrderGearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderGearViewHolder holder, int position) {
        OrderItemResponse item = items.get(position);

        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
        String name = !TextUtils.isEmpty(item.getGearName()) ? item.getGearName() : "Gear";
        holder.tvOrderGearName.setText(name + " x" + qty);

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(holder.imgOrderGear)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.camping_24px)
                    .error(R.drawable.camping_24px)
                    .centerCrop()
                    .into(holder.imgOrderGear);
        } else {
            holder.imgOrderGear.setImageResource(R.drawable.camping_24px);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderGearViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgOrderGear;
        private final TextView tvOrderGearName;

        OrderGearViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOrderGear = itemView.findViewById(R.id.imgOrderGear);
            tvOrderGearName = itemView.findViewById(R.id.tvOrderGearName);
        }
    }
}

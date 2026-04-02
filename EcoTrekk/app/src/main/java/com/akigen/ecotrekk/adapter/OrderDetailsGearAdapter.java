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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailsGearAdapter extends RecyclerView.Adapter<OrderDetailsGearAdapter.OrderDetailsGearViewHolder> {

    private final List<OrderItemResponse> items = new ArrayList<>();
    private final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.US);

    public void submitList(List<OrderItemResponse> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderDetailsGearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail_gear, parent, false);
        return new OrderDetailsGearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsGearViewHolder holder, int position) {
        OrderItemResponse item = items.get(position);

        holder.tvDetailGearName.setText(!TextUtils.isEmpty(item.getGearName()) ? item.getGearName() : "Gear Item");
        holder.tvDetailGearCategory.setText(!TextUtils.isEmpty(item.getGearCategory()) ? item.getGearCategory() : "Other");
        holder.tvDetailGearQty.setText("x" + (item.getQuantity() != null ? item.getQuantity() : 0));
        double rent = item.getRentPrice() != null ? item.getRentPrice() : 0.0;
        double deposit = item.getRefundableDeposit() != null ? item.getRefundableDeposit() : 0.0;
        holder.tvDetailGearRentPrice.setText("Rent: LKR " + currencyFormatter.format(rent) + "/day");
        holder.tvDetailGearRefundableDeposit.setText("Refundable deposit: LKR " + currencyFormatter.format(deposit));

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(holder.imgDetailGear)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.camping_24px)
                    .error(R.drawable.camping_24px)
                    .centerCrop()
                    .into(holder.imgDetailGear);
        } else {
            holder.imgDetailGear.setImageResource(R.drawable.camping_24px);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderDetailsGearViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgDetailGear;
        private final TextView tvDetailGearName;
        private final TextView tvDetailGearCategory;
        private final TextView tvDetailGearQty;
        private final TextView tvDetailGearRentPrice;
        private final TextView tvDetailGearRefundableDeposit;

        OrderDetailsGearViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDetailGear = itemView.findViewById(R.id.imgDetailGear);
            tvDetailGearName = itemView.findViewById(R.id.tvDetailGearName);
            tvDetailGearCategory = itemView.findViewById(R.id.tvDetailGearCategory);
            tvDetailGearQty = itemView.findViewById(R.id.tvDetailGearQty);
            tvDetailGearRentPrice = itemView.findViewById(R.id.tvDetailGearRentPrice);
            tvDetailGearRefundableDeposit = itemView.findViewById(R.id.tvDetailGearRefundableDeposit);
        }
    }
}

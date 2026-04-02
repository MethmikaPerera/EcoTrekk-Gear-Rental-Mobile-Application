package com.akigen.ecotrekk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.dto.OrderHistoryResponse;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderHistoryResponse order);
    }

    private final List<OrderHistoryResponse> orders = new ArrayList<>();
    private final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.US);
    private final OnOrderClickListener onOrderClickListener;

    public OrderHistoryAdapter(OnOrderClickListener onOrderClickListener) {
        this.onOrderClickListener = onOrderClickListener;
    }

    public void submitList(List<OrderHistoryResponse> list) {
        orders.clear();
        if (list != null) {
            orders.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        OrderHistoryResponse order = orders.get(position);

        String orderNumber = order.getOrderNumber() != null ? order.getOrderNumber() : String.valueOf(order.getId());
        holder.tvOrderNumber.setText("Order #" + orderNumber);
        holder.tvOrderDateRange.setText(buildDateRange(order.getStartDate(), order.getEndDate()));
        holder.tvOrderPickupHub.setText("Pickup: " + (order.getPickupHubName() != null ? order.getPickupHubName() : "Unknown Hub"));

        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        holder.tvOrderTotal.setText("LKR " + currencyFormatter.format(total));

        String status = normalizeStatus(order.getStatus());
        holder.chipOrderStatus.setText(status);
        applyStatusStyle(holder.chipOrderStatus, status);

        holder.gearAdapter.submitList(order.getItems());
        holder.itemView.setOnClickListener(v -> {
            if (onOrderClickListener != null) {
                onOrderClickListener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "Unknown";
        }

        String s = status.trim().toUpperCase(Locale.US);
        if ("COMPLETED".equals(s) || "RETURNED".equals(s)) {
            return "Returned";
        }
        if ("CANCELLED".equals(s)) {
            return "Cancelled";
        }
        return "In Progress";
    }

    private void applyStatusStyle(Chip chip, String status) {
        if ("Returned".equals(status)) {
            chip.setChipBackgroundColorResource(R.color.md_theme_secondaryContainer);
            chip.setTextColor(chip.getContext().getColor(R.color.md_theme_onSecondaryContainer));
            return;
        }
        if ("Cancelled".equals(status)) {
            chip.setChipBackgroundColorResource(R.color.md_theme_errorContainer);
            chip.setTextColor(chip.getContext().getColor(R.color.md_theme_onErrorContainer));
            return;
        }
        chip.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        chip.setTextColor(chip.getContext().getColor(R.color.md_theme_onPrimaryContainer));
    }

    private String buildDateRange(String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return "Date not available";
        }
        if (startDate == null) {
            return formatDate(endDate);
        }
        if (endDate == null) {
            return formatDate(startDate);
        }
        return formatDate(startDate) + " - " + formatDate(endDate);
    }

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat source = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat target = new SimpleDateFormat("MMM dd", Locale.US);
            return target.format(source.parse(rawDate));
        } catch (Exception ignored) {
            return rawDate;
        }
    }

    static class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderNumber;
        private final TextView tvOrderDateRange;
        private final TextView tvOrderPickupHub;
        private final TextView tvOrderTotal;
        private final Chip chipOrderStatus;
        private final RecyclerView rvOrderGearItems;
        private final OrderGearAdapter gearAdapter;

        OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderDateRange = itemView.findViewById(R.id.tvOrderDateRange);
            tvOrderPickupHub = itemView.findViewById(R.id.tvOrderPickupHub);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            chipOrderStatus = itemView.findViewById(R.id.chipOrderStatus);
            rvOrderGearItems = itemView.findViewById(R.id.rvOrderGearItems);

            gearAdapter = new OrderGearAdapter();
            rvOrderGearItems.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvOrderGearItems.setAdapter(gearAdapter);
            rvOrderGearItems.setNestedScrollingEnabled(false);
        }
    }
}

package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.adapter.OrderHistoryAdapter;
import com.akigen.ecotrekk.api.OrderApiHelper;
import com.akigen.ecotrekk.dto.OrderHistoryResponse;
import com.akigen.ecotrekk.manager.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OrderHistoryActivity extends AppCompatActivity {

    private OrderHistoryAdapter orderHistoryAdapter;
    private OrderApiHelper orderApiHelper;
    private RecyclerView rvOrderHistory;
    private TextView tvOrderHistoryEmpty;
    private ChipGroup filterChipGroup;
    private final List<OrderHistoryResponse> allOrders = new ArrayList<>();
    private final Gson gson = new Gson();
    private static final String FILTER_ALL = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupBackButton();
        fetchOrders();
    }

    private void initializeViews() {
        SessionManager sessionManager = new SessionManager(this);
        long customerId = sessionManager.getCustomerId();
        orderApiHelper = new OrderApiHelper(customerId);

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        tvOrderHistoryEmpty = findViewById(R.id.tvOrderHistoryEmpty);

        orderHistoryAdapter = new OrderHistoryAdapter(this::openOrderDetails);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(orderHistoryAdapter);

        filterChipGroup = findViewById(R.id.orderStatusChipGroup);
        if (filterChipGroup != null) {
            setupFilterListeners(filterChipGroup);
            populateStatusChips(FILTER_ALL);
        }
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.orderHistoryBackButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void fetchOrders() {
        orderApiHelper.fetchCustomerOrderHistory(new OrderApiHelper.OrderCallback<List<OrderHistoryResponse>>() {
            @Override
            public void onSuccess(List<OrderHistoryResponse> orders) {
                allOrders.clear();
                if (orders != null) {
                    allOrders.addAll(orders);
                }
                populateStatusChips(FILTER_ALL);
                applyFilter(FILTER_ALL);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OrderHistoryActivity.this, "Failed to load orders: " + message, Toast.LENGTH_SHORT).show();
                allOrders.clear();
                populateStatusChips(FILTER_ALL);
                applyFilter(FILTER_ALL);
            }
        });
    }

    private void setupFilterListeners(ChipGroup filterChipGroup) {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int selectedId = checkedIds.get(0);
                Chip selectedChip = findViewById(selectedId);
                if (selectedChip != null) {
                    String filterKey = selectedChip.getTag() instanceof String
                            ? (String) selectedChip.getTag()
                            : FILTER_ALL;
                    applyFilter(filterKey);
                }
            }
        });
    }

    private void populateStatusChips(String selectedFilterKey) {
        if (filterChipGroup == null) {
            return;
        }

        filterChipGroup.removeAllViews();

        Set<String> statusKeys = new LinkedHashSet<>();
        statusKeys.add(FILTER_ALL);
        for (OrderHistoryResponse order : allOrders) {
            String status = normalizeStatus(order != null ? order.getStatus() : null);
            if (!status.isEmpty()) {
                statusKeys.add(status);
            }
        }

        int selectedChipId = View.NO_ID;
        for (String statusKey : statusKeys) {
            Chip chip = new Chip(this);
            chip.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setText(formatStatusLabel(statusKey));
            chip.setTag(statusKey);

            filterChipGroup.addView(chip);

            if (statusKey.equalsIgnoreCase(selectedFilterKey)) {
                selectedChipId = chip.getId();
            }
        }

        if (selectedChipId == View.NO_ID && filterChipGroup.getChildCount() > 0) {
            selectedChipId = filterChipGroup.getChildAt(0).getId();
        }

        if (selectedChipId != View.NO_ID) {
            filterChipGroup.check(selectedChipId);
        }
    }

    private void applyFilter(String filterName) {
        List<OrderHistoryResponse> filtered = new ArrayList<>();
        for (OrderHistoryResponse order : allOrders) {
            if (matchesFilter(order, filterName)) {
                filtered.add(order);
            }
        }

        orderHistoryAdapter.submitList(filtered);
        boolean isEmpty = filtered.isEmpty();
        rvOrderHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvOrderHistoryEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private boolean matchesFilter(OrderHistoryResponse order, String filterName) {
        if (FILTER_ALL.equalsIgnoreCase(filterName)) {
            return true;
        }

        String status = normalizeStatus(order != null ? order.getStatus() : null);
        return status.equalsIgnoreCase(filterName);
    }

    private String normalizeStatus(String rawStatus) {
        return rawStatus == null ? "" : rawStatus.trim().toUpperCase(Locale.US);
    }

    private String formatStatusLabel(String statusKey) {
        if (FILTER_ALL.equalsIgnoreCase(statusKey)) {
            return "All";
        }

        String normalized = statusKey == null ? "" : statusKey.trim().replace('_', ' ');
        if (normalized.isEmpty()) {
            return "Unknown";
        }

        String[] words = normalized.toLowerCase(Locale.US).split("\\s+");
        StringBuilder labelBuilder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (labelBuilder.length() > 0) {
                labelBuilder.append(' ');
            }
            labelBuilder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return labelBuilder.toString();
    }

    private void openOrderDetails(OrderHistoryResponse order) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_NUMBER, order.getOrderNumber());
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_STATUS, order.getStatus());
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_START_DATE, order.getStartDate());
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_END_DATE, order.getEndDate());
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_PICKUP_HUB, order.getPickupHubName());
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_PICKUP_HUB_LATITUDE, order.getPickupHubLatitude() != null ? order.getPickupHubLatitude() : 0.0);
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_PICKUP_HUB_LONGITUDE, order.getPickupHubLongitude() != null ? order.getPickupHubLongitude() : 0.0);
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_SUBTOTAL, order.getSubtotal() != null ? order.getSubtotal() : 0.0);
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_REFUNDABLE_DEPOSIT, order.getRefundableDeposit() != null ? order.getRefundableDeposit() : 0.0);
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_TOTAL, order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
        intent.putExtra(OrderDetailsActivity.EXTRA_ORDER_ITEMS_JSON, gson.toJson(order.getItems()));
        startActivity(intent);
    }
}
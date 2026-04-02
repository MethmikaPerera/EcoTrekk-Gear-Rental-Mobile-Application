package com.akigen.ecotrekk.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.adapter.OrderDetailsGearAdapter;
import com.akigen.ecotrekk.dto.OrderItemResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_NUMBER = "extra_order_number";
    public static final String EXTRA_ORDER_STATUS = "extra_order_status";
    public static final String EXTRA_ORDER_START_DATE = "extra_order_start_date";
    public static final String EXTRA_ORDER_END_DATE = "extra_order_end_date";
    public static final String EXTRA_ORDER_PICKUP_HUB = "extra_order_pickup_hub";
    public static final String EXTRA_ORDER_PICKUP_HUB_LATITUDE = "extra_order_pickup_hub_latitude";
    public static final String EXTRA_ORDER_PICKUP_HUB_LONGITUDE = "extra_order_pickup_hub_longitude";
    public static final String EXTRA_ORDER_SUBTOTAL = "extra_order_subtotal";
    public static final String EXTRA_ORDER_REFUNDABLE_DEPOSIT = "extra_order_refundable_deposit";
    public static final String EXTRA_ORDER_TOTAL = "extra_order_total";
    public static final String EXTRA_ORDER_ITEMS_JSON = "extra_order_items_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackButton();
        bindOrderDetails();
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.orderDetailsBackButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void bindOrderDetails() {
        String orderNumber = getIntent().getStringExtra(EXTRA_ORDER_NUMBER);
        String statusRaw = getIntent().getStringExtra(EXTRA_ORDER_STATUS);
        String startDate = getIntent().getStringExtra(EXTRA_ORDER_START_DATE);
        String endDate = getIntent().getStringExtra(EXTRA_ORDER_END_DATE);
        String pickupHub = getIntent().getStringExtra(EXTRA_ORDER_PICKUP_HUB);
        double pickupHubLatitude = getIntent().getDoubleExtra(EXTRA_ORDER_PICKUP_HUB_LATITUDE, 0.0);
        double pickupHubLongitude = getIntent().getDoubleExtra(EXTRA_ORDER_PICKUP_HUB_LONGITUDE, 0.0);
        double subtotal = getIntent().getDoubleExtra(EXTRA_ORDER_SUBTOTAL, 0.0);
        double refundableDeposit = getIntent().getDoubleExtra(EXTRA_ORDER_REFUNDABLE_DEPOSIT, 0.0);
        double totalAmount = getIntent().getDoubleExtra(EXTRA_ORDER_TOTAL, 0.0);
        String itemsJson = getIntent().getStringExtra(EXTRA_ORDER_ITEMS_JSON);

        TextView tvDetailOrderNumber = findViewById(R.id.tvDetailOrderNumber);
        Chip chipDetailOrderStatus = findViewById(R.id.chipDetailOrderStatus);
        TextView tvDetailOrderDateRange = findViewById(R.id.tvDetailOrderDateRange);
        TextView tvDetailPickupHub = findViewById(R.id.tvDetailPickupHub);
        MaterialButton btnDetailHubDirections = findViewById(R.id.btnDetailHubDirections);
        TextView tvDetailOrderRentalDays = findViewById(R.id.tvDetailOrderRentalDays);
        TextView tvDetailOrderSubtotal = findViewById(R.id.tvDetailOrderSubtotal);
        TextView tvDetailOrderRefundableDeposit = findViewById(R.id.tvDetailOrderRefundableDeposit);
        TextView tvDetailOrderTotal = findViewById(R.id.tvDetailOrderTotal);
        RecyclerView rvOrderDetailItems = findViewById(R.id.rvOrderDetailItems);

        String safeOrderNumber = orderNumber != null ? orderNumber : "N/A";
        tvDetailOrderNumber.setText("Order #" + safeOrderNumber);

        String normalizedStatus = normalizeStatus(statusRaw);
        chipDetailOrderStatus.setText(normalizedStatus);
        applyStatusStyle(chipDetailOrderStatus, normalizedStatus);

        tvDetailOrderDateRange.setText(buildDateRange(startDate, endDate));
        tvDetailPickupHub.setText("Pickup: " + (pickupHub != null ? pickupHub : "Unknown Hub"));
        if (btnDetailHubDirections != null) {
            btnDetailHubDirections.setOnClickListener(v -> openDirections(pickupHub, pickupHubLatitude, pickupHubLongitude));
            btnDetailHubDirections.setEnabled(isValidHubLocation(pickupHubLatitude, pickupHubLongitude));
        }
        tvDetailOrderRentalDays.setText(formatRentalDays(startDate, endDate));

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        tvDetailOrderSubtotal.setText("LKR " + formatter.format(subtotal));
        tvDetailOrderRefundableDeposit.setText("LKR " + formatter.format(refundableDeposit));
        tvDetailOrderTotal.setText("LKR " + formatter.format(totalAmount));

        OrderDetailsGearAdapter adapter = new OrderDetailsGearAdapter();
        rvOrderDetailItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetailItems.setAdapter(adapter);

        Type listType = new TypeToken<List<OrderItemResponse>>() { }.getType();
        List<OrderItemResponse> items = new Gson().fromJson(itemsJson, listType);
        if (items == null) {
            items = new ArrayList<>();
        }
        adapter.submitList(items);
    }

    private boolean isValidHubLocation(double latitude, double longitude) {
        boolean latitudeValid = latitude >= -90d && latitude <= 90d;
        boolean longitudeValid = longitude >= -180d && longitude <= 180d;
        boolean hasExactCoordinate = Math.abs(latitude) > 0.000001d || Math.abs(longitude) > 0.000001d;
        return latitudeValid && longitudeValid && hasExactCoordinate;
    }

    private void openDirections(String hubName, double hubLatitude, double hubLongitude) {
        if (!isValidHubLocation(hubLatitude, hubLongitude)) {
            return;
        }

        String query = hubLatitude + "," + hubLongitude;
        Uri navigationUri = Uri.parse("google.navigation:q=" + query);
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigationUri);
        navigationIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(navigationIntent);
        } catch (ActivityNotFoundException ignored) {
            String safeHubName = hubName == null ? "Pickup Hub" : hubName;
            Uri geoUri = Uri.parse("geo:0,0?q=" + query + "(" + Uri.encode(safeHubName) + ")");
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            if (geoIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(geoIntent);
            }
        }
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

    private String formatRentalDays(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return "N/A";
        }

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long days = Math.max(1L, ChronoUnit.DAYS.between(start, end) + 1L);
            return days + (days == 1L ? " day" : " days");
        } catch (Exception ignored) {
            return "N/A";
        }
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
}
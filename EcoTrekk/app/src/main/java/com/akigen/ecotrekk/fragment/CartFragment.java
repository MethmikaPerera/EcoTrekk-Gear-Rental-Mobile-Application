package com.akigen.ecotrekk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.CheckoutActivity;
import com.akigen.ecotrekk.adapter.CartItemAdapter;
import com.akigen.ecotrekk.api.CartApiHelper;
import com.akigen.ecotrekk.database.CartHelper;
import com.akigen.ecotrekk.database.entity.CartItemEntity;
import com.akigen.ecotrekk.databinding.FragmentCartBinding;
import com.akigen.ecotrekk.dto.CartItemResponse;
import com.akigen.ecotrekk.manager.SessionManager;
import com.akigen.ecotrekk.model.CartItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CartFragment extends Fragment {
    private static final int DEFAULT_RENTAL_DAYS = 2;

    private FragmentCartBinding binding;
    private CartItemAdapter cartItemAdapter;
    private final List<CartItem> cartItems = new ArrayList<>();
    private long selectedRangeStartUtc = -1L;
    private long selectedRangeEndUtc = -1L;
    private boolean isPaymentSummaryExpanded = false;
    private SessionManager sessionManager;
    private CartApiHelper cartApiHelper;
    private CartHelper cartHelper;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackPressNavigation();
        setupRecyclerView();
        setupActions();
        setupPaymentSummaryToggle();
        updateDateRangeDisplay(DEFAULT_RENTAL_DAYS);
        updateCheckoutButtonState();
        initializeCartDataSource();
        observeLocalCartItems();
        loadCartItemsFromBackend();
    }

    private void observeLocalCartItems() {
        if (cartHelper == null) {
            return;
        }

        cartHelper.observeCartItems().observe(getViewLifecycleOwner(), localItems -> {
            if (localItems == null || !isAdded() || binding == null) {
                return;
            }

            List<CartItem> mappedItems = new ArrayList<>();
            long localStart = selectedRangeStartUtc;
            long localEnd = selectedRangeEndUtc;

            for (CartItemEntity entity : localItems) {
                if (localStart <= 0 && entity.getStartDate() > 0) {
                    localStart = entity.getStartDate();
                }
                if (localEnd <= 0 && entity.getEndDate() > 0) {
                    localEnd = entity.getEndDate();
                }

                int rentalDays = (int) Math.max(1, entity.getRentalDays());
                mappedItems.add(new CartItem(
                        entity.getId(),
                        entity.getGearId(),
                        entity.getHubId(),
                        entity.getGearName(),
                        entity.getCategory(),
                        Math.max(1, entity.getQuantity()),
                        rentalDays,
                        entity.getPricePerDay(),
                        entity.getDepositAmount(),
                        entity.getImageUrl(),
                        resolveCartImage(entity.getCategory())
                ));
            }

            cartItems.clear();
            cartItems.addAll(mappedItems);
            selectedRangeStartUtc = localStart;
            selectedRangeEndUtc = localEnd;

            int rentalDays = areDatesSelected()
                    ? calculateRentalDays(selectedRangeStartUtc, selectedRangeEndUtc)
                    : DEFAULT_RENTAL_DAYS;

            applySharedRentalDays(rentalDays);
            updateDateRangeDisplay(rentalDays);
            renderCartState();
        });
    }

    private void initializeCartDataSource() {
        sessionManager = new SessionManager(requireContext());
        cartHelper = CartHelper.getInstance(requireContext());
        long customerId = sessionManager.getCustomerId();
        if (customerId > 0) {
            cartApiHelper = new CartApiHelper(customerId);
        }
    }

    private void setupBackPressNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.homeActivityBottomNavigationView);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
                    return;
                }

                setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        cartItemAdapter = new CartItemAdapter(new CartItemAdapter.OnCartItemActionListener() {
            @Override
            public void onCartChanged() {
                updateSummary();
                syncLocalCartSnapshot();
            }

            @Override
            public void onItemRemoved(int position, CartItem item) {
                if (position < 0 || position >= cartItems.size()) {
                    return;
                }
                if (cartApiHelper != null && item.getCartItemId() > 0) {
                    cartApiHelper.removeCartItem(item.getCartItemId(), new CartApiHelper.CartCallback<>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (!isAdded() || binding == null) {
                                return;
                            }
                            requireActivity().runOnUiThread(() -> {
                                cartItems.remove(position);
                                if (item.getGearId() > 0) {
                                    cartHelper.removeGear(item.getGearId());
                                }
                                renderCartState();
                                Snackbar.make(binding.getRoot(), R.string.cart_item_removed, Snackbar.LENGTH_LONG)
                                        .setAnchorView(binding.cardOrderSummary)
                                        .show();
                            });
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded() || binding == null) {
                                return;
                            }
                            requireActivity().runOnUiThread(() -> Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                                    .setAnchorView(binding.cardOrderSummary)
                                    .show());
                        }
                    });
                    return;
                }

                cartItems.remove(position);
                if (item.getGearId() > 0) {
                    cartHelper.removeGear(item.getGearId());
                }
                renderCartState();
                Snackbar.make(binding.getRoot(), R.string.cart_item_removed, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.cardOrderSummary)
                        .show();
            }
        });
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCartItems.setAdapter(cartItemAdapter);
    }

    private void setupActions() {
        binding.btnBrowseGear.setOnClickListener(v -> {
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.homeActivityBottomNavigationView);
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.bottom_nav_gears);
            }
        });

        binding.btnSelectCartDateRange.setOnClickListener(v -> showDateRangePicker());

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Snackbar.make(binding.getRoot(), R.string.cart_checkout_empty_state, Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.cardOrderSummary)
                        .show();
                return;
            }

            if (!areDatesSelected()) {
                Snackbar.make(binding.getRoot(), R.string.cart_checkout_dates_required, Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.cardOrderSummary)
                        .show();
                return;
            }

            launchCheckout();
        });
    }

    private void launchCheckout() {
        Intent checkoutIntent = new Intent(requireContext(), CheckoutActivity.class);
        checkoutIntent.putExtra(CheckoutActivity.EXTRA_SELECTED_RANGE_START_UTC, selectedRangeStartUtc);
        checkoutIntent.putExtra(CheckoutActivity.EXTRA_SELECTED_RANGE_END_UTC, selectedRangeEndUtc);
        checkoutIntent.putExtra(CheckoutActivity.EXTRA_CART_ITEM_COUNT, calculateTotalUnits());
        checkoutIntent.putExtra(CheckoutActivity.EXTRA_CART_SUBTOTAL, calculateSubTotal());
        startActivity(checkoutIntent);
    }

    private void setupPaymentSummaryToggle() {
        binding.layoutPaymentSummaryHeader.setOnClickListener(v -> {
            isPaymentSummaryExpanded = !isPaymentSummaryExpanded;
            updatePaymentSummaryCollapseState();
        });
        updatePaymentSummaryCollapseState();
    }

    private void updatePaymentSummaryCollapseState() {
        binding.layoutPaymentSummaryContent.setVisibility(isPaymentSummaryExpanded ? View.VISIBLE : View.GONE);
        binding.imgPaymentSummaryToggle.setImageResource(
                isPaymentSummaryExpanded ? R.drawable.expand_more_24px : R.drawable.expand_less_24px
        );
        binding.imgPaymentSummaryToggle.setContentDescription(getString(
                isPaymentSummaryExpanded
                        ? R.string.cart_collapse_payment_summary
                        : R.string.cart_expand_payment_summary
        ));
    }

    private void loadCartItemsFromBackend() {
        if (cartApiHelper == null) {
            loadCartItemsFromLocal();
            return;
        }

        cartApiHelper.fetchCartItems(new CartApiHelper.CartCallback<>() {
            @Override
            public void onSuccess(List<CartItemResponse> result) {
                cartItems.clear();

                if (result == null || result.isEmpty()) {
                    selectedRangeStartUtc = -1L;
                    selectedRangeEndUtc = -1L;
                    updateDateRangeDisplay(DEFAULT_RENTAL_DAYS);
                    renderCartState();
                    return;
                }

                Long parsedStartUtc = parseIsoDateToUtcMillis(result.get(0).getStartDate());
                Long parsedEndUtc = parseIsoDateToUtcMillis(result.get(0).getEndDate());
                if (parsedStartUtc != null && parsedEndUtc != null && parsedEndUtc >= parsedStartUtc) {
                    selectedRangeStartUtc = parsedStartUtc;
                    selectedRangeEndUtc = parsedEndUtc;
                } else {
                    selectedRangeStartUtc = -1L;
                    selectedRangeEndUtc = -1L;
                }

                int rentalDays = areDatesSelected()
                        ? calculateRentalDays(selectedRangeStartUtc, selectedRangeEndUtc)
                        : DEFAULT_RENTAL_DAYS;

                for (CartItemResponse dto : result) {
                    String name = dto.getGearName() != null ? dto.getGearName() : "Cart Item";
                    String category = dto.getCategory() != null ? dto.getCategory() : "Other";
                    int quantity = dto.getQuantity() != null ? Math.max(1, dto.getQuantity()) : 1;
                    double pricePerDay = dto.getPricePerDay() != null ? dto.getPricePerDay() : 0.0;
                    double depositAmount = dto.getDepositAmount() != null ? dto.getDepositAmount() : 0.0;
                    String imageUrl = dto.getImageUrl();
                    int imageResId = resolveCartImage(category);
                    long cartItemId = dto.getCartItemId() != null ? dto.getCartItemId() : -1L;
                    long gearId = dto.getGearId() != null ? dto.getGearId() : -1L;
                    long hubId = dto.getHubId() != null ? dto.getHubId() : -1L;
                    int availableQty = dto.getAvailableQty() != null ? dto.getAvailableQty() : Integer.MAX_VALUE;

                    cartItems.add(new CartItem(cartItemId, gearId, hubId, name, category, quantity, rentalDays, pricePerDay, depositAmount, imageUrl, imageResId, availableQty));
                }

                syncLocalCartFromBackend(result);

                updateDateRangeDisplay(rentalDays);
                renderCartState();
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || binding == null) {
                    return;
                }
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.cardOrderSummary)
                        .show();
                loadCartItemsFromLocal();
            }
        });
    }

    private void loadCartItemsFromLocal() {
        if (cartHelper == null) {
            cartItems.clear();
            selectedRangeStartUtc = -1L;
            selectedRangeEndUtc = -1L;
            updateDateRangeDisplay(DEFAULT_RENTAL_DAYS);
            renderCartState();
            return;
        }

        ioExecutor.execute(() -> {
            List<CartItemEntity> localItems = cartHelper.getCartItems();
            List<CartItem> mappedItems = new ArrayList<>();

            long localStart = -1L;
            long localEnd = -1L;

            for (CartItemEntity entity : localItems) {
                if (localStart <= 0 && entity.getStartDate() > 0) {
                    localStart = entity.getStartDate();
                }
                if (localEnd <= 0 && entity.getEndDate() > 0) {
                    localEnd = entity.getEndDate();
                }

                int rentalDays = (int) Math.max(1, entity.getRentalDays());
                mappedItems.add(new CartItem(
                    entity.getId(),
                        entity.getGearId(),
                        entity.getHubId(),
                        entity.getGearName(),
                        entity.getCategory(),
                        Math.max(1, entity.getQuantity()),
                        rentalDays,
                        entity.getPricePerDay(),
                    entity.getDepositAmount(),
                    entity.getImageUrl(),
                        resolveCartImage(entity.getCategory())
                ));
            }

            long finalLocalStart = localStart;
            long finalLocalEnd = localEnd;
            if (!isAdded() || binding == null) {
                return;
            }

            requireActivity().runOnUiThread(() -> {
                cartItems.clear();
                cartItems.addAll(mappedItems);

                selectedRangeStartUtc = finalLocalStart;
                selectedRangeEndUtc = finalLocalEnd;

                int rentalDays = areDatesSelected()
                        ? calculateRentalDays(selectedRangeStartUtc, selectedRangeEndUtc)
                        : DEFAULT_RENTAL_DAYS;

                applySharedRentalDays(rentalDays);
                updateDateRangeDisplay(rentalDays);
                renderCartState();
            });
        });
    }

    private void syncLocalCartFromBackend(List<CartItemResponse> result) {
        if (cartHelper == null || result == null) {
            return;
        }

        ioExecutor.execute(() -> {
            List<CartItemEntity> localItems = new ArrayList<>();

            for (CartItemResponse dto : result) {
                long gearId = dto.getGearId() != null ? dto.getGearId() : -1L;
                if (gearId <= 0) {
                    continue;
                }

                String name = dto.getGearName() != null ? dto.getGearName() : "Cart Item";
                String category = dto.getCategory() != null ? dto.getCategory() : "Other";
                int quantity = dto.getQuantity() != null ? Math.max(1, dto.getQuantity()) : 1;
                double pricePerDay = dto.getPricePerDay() != null ? dto.getPricePerDay() : 0.0;
                double depositAmount = dto.getDepositAmount() != null ? dto.getDepositAmount() : 0.0;
                long hubId = dto.getHubId() != null ? dto.getHubId() : 1L;
                String hubName = dto.getHubName() != null ? dto.getHubName() : ("Hub " + hubId);

                long startMillis = parseIsoDateToUtcMillis(dto.getStartDate()) != null
                        ? parseIsoDateToUtcMillis(dto.getStartDate())
                        : 0L;
                long endMillis = parseIsoDateToUtcMillis(dto.getEndDate()) != null
                        ? parseIsoDateToUtcMillis(dto.getEndDate())
                        : 0L;

                localItems.add(new CartItemEntity(
                        gearId,
                        name,
                        category,
                        quantity,
                        startMillis,
                        endMillis,
                        pricePerDay,
                        depositAmount,
                        hubId,
                        hubName,
                        dto.getImageUrl() != null ? dto.getImageUrl() : ""
                ));
            }

            cartHelper.replaceCartItems(localItems);
        });
    }

    private void syncLocalCartSnapshot() {
        if (cartHelper == null) {
            return;
        }

        ioExecutor.execute(() -> {
            List<CartItemEntity> snapshotItems = new ArrayList<>();

            long startMillis = selectedRangeStartUtc > 0 ? selectedRangeStartUtc : 0L;
            long endMillis = selectedRangeEndUtc > 0 ? selectedRangeEndUtc : 0L;

            for (CartItem item : cartItems) {
                if (item.getGearId() <= 0) {
                    continue;
                }

                long hubId = item.getHubId() > 0 ? item.getHubId() : 1L;
                double depositAmount = Math.max(0d, item.getDepositAmount());

                snapshotItems.add(new CartItemEntity(
                        item.getGearId(),
                        item.getName(),
                        item.getCategory(),
                        item.getQuantity(),
                        startMillis,
                        endMillis,
                        item.getPerDayPrice(),
                        depositAmount,
                        hubId,
                        "Hub " + hubId,
                        item.getImageUrl() != null ? item.getImageUrl() : ""
                ));
            }

            cartHelper.replaceCartItems(snapshotItems);
        });
    }

    private Long parseIsoDateToUtcMillis(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(isoDate);
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (Exception ignored) {
            return null;
        }
    }

    private int resolveCartImage(String category) {
        if (category == null) {
            return R.drawable.ecotrekk_notfound;
        }

        String normalized = category.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("footwear") || normalized.contains("shoe") || normalized.contains("boot")) {
            return R.drawable.ecotrekk_notfound;
        }
        return R.drawable.ecotrekk_notfound;
    }

    private void showDateRangePicker() {
        long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();
        CalendarConstraints constraints = new CalendarConstraints.Builder()
            .setStart(todayUtc)
            .setValidator(DateValidatorPointForward.from(todayUtc))
            .build();

        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.cart_date_range_picker_title)
            .setCalendarConstraints(constraints);

        if (selectedRangeStartUtc > 0 && selectedRangeEndUtc > 0) {
            builder.setSelection(new androidx.core.util.Pair<>(selectedRangeStartUtc, selectedRangeEndUtc));
        }

        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }

            selectedRangeStartUtc = selection.first;
            selectedRangeEndUtc = selection.second;

            int rentalDays = calculateRentalDays(selectedRangeStartUtc, selectedRangeEndUtc);
            applySharedRentalDays(rentalDays);
            updateDateRangeDisplay(rentalDays);
            updateCheckoutButtonState();
            renderCartState();
        });

        picker.show(getChildFragmentManager(), "cart_date_range_picker");
    }

    private int calculateRentalDays(long startUtc, long endUtc) {
        long diffMillis = Math.max(0L, endUtc - startUtc);
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis) + 1L;
        return (int) Math.max(1L, diffDays);
    }

    private void applySharedRentalDays(int rentalDays) {
        for (CartItem cartItem : cartItems) {
            cartItem.setRentalDays(rentalDays);
        }
    }

    private void updateDateRangeDisplay(int rentalDays) {
        if (selectedRangeStartUtc <= 0 || selectedRangeEndUtc <= 0) {
            binding.tvCartDateRange.setText(getString(R.string.cart_date_range_placeholder));
            return;
        }

        CharSequence fromDate = DateFormat.format("dd MMM yyyy", new Date(selectedRangeStartUtc));
        CharSequence toDate = DateFormat.format("dd MMM yyyy", new Date(selectedRangeEndUtc));
        binding.tvCartDateRange.setText(getString(
                R.string.cart_date_range_format,
                fromDate,
                toDate,
                rentalDays
        ));
    }

    private boolean areDatesSelected() {
        return selectedRangeStartUtc > 0 && selectedRangeEndUtc > 0;
    }

    private void updateCheckoutButtonState() {
        boolean datesSelected = areDatesSelected();
        binding.btnCheckout.setEnabled(datesSelected);
        binding.tvCartDateRangeAlert.setVisibility(datesSelected ? View.GONE : View.VISIBLE);
    }

    private void renderCartState() {
        boolean isEmpty = cartItems.isEmpty();
        binding.cardEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvCartItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.cardOrderSummary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        cartItemAdapter.submitList(cartItems);
        updateCheckoutButtonState();
        updateSummary();
    }

    private void updateSummary() {
        double subtotal = calculateSubTotal();
        double refundableDeposit = calculateRefundableDeposit();
        double totalAmount = subtotal + refundableDeposit;

        binding.tvCartSubtotal.setText(formatCurrency(subtotal));
        binding.tvCartServiceFee.setText(formatCurrency(refundableDeposit));
        binding.tvCartGrandTotal.setText(formatCurrency(totalAmount));
        binding.btnCheckout.setText(R.string.cart_checkout_cta);
    }

    private int calculateTotalUnits() {
        int totalUnits = 0;
        for (CartItem cartItem : cartItems) {
            totalUnits += cartItem.getQuantity();
        }
        return totalUnits;
    }

    private double calculateSubTotal() {
        double subtotal = 0d;
        for (CartItem cartItem : cartItems) {
            subtotal += cartItem.getLineTotal();
        }
        return subtotal;
    }

    private double calculateRefundableDeposit() {
        double depositTotal = 0d;
        for (CartItem cartItem : cartItems) {
            depositTotal += Math.max(0d, cartItem.getDepositAmount()) * Math.max(1, cartItem.getQuantity());
        }
        return depositTotal;
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "LKR %,.2f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ioExecutor.shutdown();
        binding = null;
    }
}
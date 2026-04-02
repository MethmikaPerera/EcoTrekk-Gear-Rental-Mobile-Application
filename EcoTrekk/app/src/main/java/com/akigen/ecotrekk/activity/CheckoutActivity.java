package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.api.CartApiHelper;
import com.akigen.ecotrekk.api.HubApiHelper;
import com.akigen.ecotrekk.api.StripeApiHelper;
import com.akigen.ecotrekk.database.CartHelper;
import com.akigen.ecotrekk.database.entity.CartItemEntity;
import com.akigen.ecotrekk.dto.CashOrderResponse;
import com.akigen.ecotrekk.dto.CartItemResponse;
import com.akigen.ecotrekk.dto.PublicHubResponse;
import com.akigen.ecotrekk.dto.StripePaymentIntentResponse;
import com.akigen.ecotrekk.manager.SessionManager;
import com.stripe.android.PaymentConfiguration;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_RANGE_START_UTC = "extra_selected_range_start_utc";
    public static final String EXTRA_SELECTED_RANGE_END_UTC = "extra_selected_range_end_utc";
    public static final String EXTRA_CART_ITEM_COUNT = "extra_cart_item_count";
    public static final String EXTRA_CART_SUBTOTAL = "extra_cart_subtotal";

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final List<CartItemEntity> checkoutItems = new ArrayList<>();

    private CartHelper cartHelper;
    private CartApiHelper cartApiHelper;
    private HubApiHelper hubApiHelper;
    private SessionManager sessionManager;
    private StripeApiHelper stripeApiHelper;
    private PaymentSheet paymentSheet;

    private boolean paymentInProgress;

    private long selectedRangeStartUtc = -1L;
    private long selectedRangeEndUtc = -1L;

    private ImageButton btnCheckoutBack;
    private MaterialButton btnPlaceOrder;
    private MaterialAutoCompleteTextView actvCollectingHub;
    private MaterialRadioButton radioCheckoutCash;

    private TextInputLayout inputLayoutCollectingHub;
    private TextInputLayout inputLayoutBillingName;
    private TextInputLayout inputLayoutBillingEmail;
    private TextInputLayout inputLayoutBillingPhone;
    private TextInputLayout inputLayoutBillingAddress;

    private TextInputEditText etBillingName;
    private TextInputEditText etBillingEmail;
    private TextInputEditText etBillingPhone;
    private TextInputEditText etBillingAddress;

    private TextView txtCheckoutSubtotal;
    private TextView txtCheckoutDeliveryFee;
    private TextView txtCheckoutTotal;
    private TextView txtCheckoutFooterTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        selectedRangeStartUtc = getIntent().getLongExtra(EXTRA_SELECTED_RANGE_START_UTC, -1L);
        selectedRangeEndUtc = getIntent().getLongExtra(EXTRA_SELECTED_RANGE_END_UTC, -1L);

        cartHelper = CartHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        long customerId = sessionManager.getCustomerId();
        if (customerId > 0) {
            cartApiHelper = new CartApiHelper(customerId);
        }
        hubApiHelper = new HubApiHelper();
        stripeApiHelper = new StripeApiHelper(sessionManager.getCustomerId());
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        bindViews();
        setupActions();
        prefillBillingDetails();
        loadCheckoutCartData();
        loadHubsForDropdown();
    }

    private void bindViews() {
        btnCheckoutBack = findViewById(R.id.btnCheckoutBack);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        actvCollectingHub = findViewById(R.id.actvCollectingHub);
        radioCheckoutCash = findViewById(R.id.radioCheckoutCash);

        inputLayoutCollectingHub = findViewById(R.id.inputLayoutCollectingHub);
        inputLayoutBillingName = findViewById(R.id.inputLayoutBillingName);
        inputLayoutBillingEmail = findViewById(R.id.inputLayoutBillingEmail);
        inputLayoutBillingPhone = findViewById(R.id.inputLayoutBillingPhone);
        inputLayoutBillingAddress = findViewById(R.id.inputLayoutBillingAddress);

        etBillingName = findViewById(R.id.etBillingName);
        etBillingEmail = findViewById(R.id.etBillingEmail);
        etBillingPhone = findViewById(R.id.etBillingPhone);
        etBillingAddress = findViewById(R.id.etBillingAddress);

        txtCheckoutSubtotal = findViewById(R.id.txtCheckoutSubtotal);
        txtCheckoutDeliveryFee = findViewById(R.id.txtCheckoutDeliveryFee);
        txtCheckoutTotal = findViewById(R.id.txtCheckoutTotal);
        txtCheckoutFooterTotal = findViewById(R.id.txtCheckoutFooterTotal);
    }

    private void setupActions() {
        btnCheckoutBack.setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void prefillBillingDetails() {
        etBillingName.setText(sessionManager.getFullName());
        etBillingEmail.setText(sessionManager.getEmail());
        etBillingPhone.setText(sessionManager.getPhone());
    }

    private void loadCheckoutCartData() {
        if (cartApiHelper != null) {
            cartApiHelper.fetchCartItems(new CartApiHelper.CartCallback<>() {
                @Override
                public void onSuccess(List<CartItemResponse> result) {
                    runOnUiThread(() -> renderCheckoutData(mapBackendItems(result)));
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT).show());
                    loadCheckoutCartDataFromLocal();
                }
            });
            return;
        }

        loadCheckoutCartDataFromLocal();
    }

    private void loadCheckoutCartDataFromLocal() {
        ioExecutor.execute(() -> {
            List<CartItemEntity> items = cartHelper != null ? cartHelper.getCartItems() : new ArrayList<>();
            List<CartItemEntity> snapshot = items == null ? new ArrayList<>() : new ArrayList<>(items);

            if ((selectedRangeStartUtc <= 0 || selectedRangeEndUtc <= 0) && !snapshot.isEmpty()) {
                CartItemEntity firstItem = snapshot.get(0);
                if (selectedRangeStartUtc <= 0) {
                    selectedRangeStartUtc = firstItem.getStartDate();
                }
                if (selectedRangeEndUtc <= 0) {
                    selectedRangeEndUtc = firstItem.getEndDate();
                }
            }

            runOnUiThread(() -> renderCheckoutData(snapshot));
        });
    }

    private List<CartItemEntity> mapBackendItems(List<CartItemResponse> result) {
        List<CartItemEntity> mapped = new ArrayList<>();
        if (result == null) {
            return mapped;
        }

        for (CartItemResponse dto : result) {
            CartItemEntity item = new CartItemEntity();
            item.setId(dto.getCartItemId() != null ? dto.getCartItemId() : 0L);
            item.setGearId(dto.getGearId() != null ? dto.getGearId() : -1L);
            item.setGearName(dto.getGearName() != null ? dto.getGearName() : "Cart Item");
            item.setCategory(dto.getCategory() != null ? dto.getCategory() : "Other");
            item.setQuantity(dto.getQuantity() != null ? Math.max(1, dto.getQuantity()) : 1);
            item.setPricePerDay(dto.getPricePerDay() != null ? dto.getPricePerDay() : 0d);
            item.setDepositAmount(dto.getDepositAmount() != null ? dto.getDepositAmount() : 0d);
            item.setHubId(dto.getHubId() != null ? dto.getHubId() : -1L);
            item.setHubName(
                    dto.getHubName() != null ? dto.getHubName() : getString(R.string.checkout_default_hub_name));
            item.setImageUrl(dto.getImageUrl());

            Long parsedStart = parseIsoDateToUtcMillis(dto.getStartDate());
            Long parsedEnd = parseIsoDateToUtcMillis(dto.getEndDate());
            item.setStartDate(parsedStart != null ? parsedStart : 0L);
            item.setEndDate(parsedEnd != null ? parsedEnd : 0L);
            item.setAddedAt(dto.getAddedAt() != null ? dto.getAddedAt() : System.currentTimeMillis());

            mapped.add(item);
        }

        return mapped;
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

    private void renderCheckoutData(List<CartItemEntity> items) {
        checkoutItems.clear();
        checkoutItems.addAll(items);

        updateCollectingHubOptions(items);
        updateTotals(items);

        boolean hasItems = !items.isEmpty();
        btnPlaceOrder.setEnabled(hasItems);
        if (!hasItems) {
            Snackbar.make(findViewById(R.id.main), R.string.checkout_empty_cart, Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateCollectingHubOptions(List<CartItemEntity> items) {
        Set<String> hubs = new LinkedHashSet<>();
        for (CartItemEntity item : items) {
            if (!TextUtils.isEmpty(item.getHubName())) {
                hubs.add(item.getHubName());
            }
        }

        if (hubs.isEmpty()) {
            hubs.add(getString(R.string.checkout_default_hub_name));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(hubs));
        actvCollectingHub.setAdapter(adapter);

        if (TextUtils.isEmpty(actvCollectingHub.getText())) {
            actvCollectingHub.setText(adapter.getItem(0), false);
        }
    }

    private void loadHubsForDropdown() {
        if (hubApiHelper != null) {
            hubApiHelper.fetchPublicHubs(new HubApiHelper.HubCallback<List<PublicHubResponse>>() {
                @Override
                public void onSuccess(List<PublicHubResponse> hubs) {
                    runOnUiThread(() -> populateHubsDropdown(hubs));
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void populateHubsDropdown(List<PublicHubResponse> hubs) {
        List<String> hubNames = new ArrayList<>();
        if (hubs != null && !hubs.isEmpty()) {
            for (PublicHubResponse hub : hubs) {
                if (!TextUtils.isEmpty(hub.getName())) {
                    hubNames.add(hub.getName());
                }
            }
        }

        if (hubNames.isEmpty()) {
            hubNames.add(getString(R.string.checkout_default_hub_name));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                hubNames);
        actvCollectingHub.setAdapter(adapter);

        if (TextUtils.isEmpty(actvCollectingHub.getText())) {
            actvCollectingHub.setText(adapter.getItem(0), false);
        }
    }

    private void updateTotals(List<CartItemEntity> items) {
        double subtotal = 0d;
        for (CartItemEntity item : items) {
            subtotal += calculateLineTotal(item);
        }
        double depositAmount = calculateDepositAmount(items);
        double total = subtotal + depositAmount;

        txtCheckoutSubtotal.setText(formatCurrency(subtotal));
        txtCheckoutDeliveryFee.setText(formatCurrency(depositAmount));
        txtCheckoutTotal.setText(formatCurrency(total));
        txtCheckoutFooterTotal.setText(getString(R.string.checkout_footer_total_format, formatCurrency(total)));
    }

    private double calculateDepositAmount(List<CartItemEntity> items) {
        double depositTotal = 0d;
        for (CartItemEntity item : items) {
            double itemDeposit = item.getDepositAmount();
            if (itemDeposit > 0) {
                depositTotal += itemDeposit * Math.max(1, item.getQuantity());
            }
        }
        return depositTotal;
    }

    private double calculateLineTotal(CartItemEntity item) {
        int rentalDays = resolveRentalDays(item);
        return item.getPricePerDay() * Math.max(1, item.getQuantity()) * rentalDays;
    }

    private int resolveRentalDays(CartItemEntity item) {
        long start = selectedRangeStartUtc > 0 ? selectedRangeStartUtc : item.getStartDate();
        long end = selectedRangeEndUtc > 0 ? selectedRangeEndUtc : item.getEndDate();

        if (start > 0 && end >= start) {
            long diffDays = TimeUnit.MILLISECONDS.toDays(end - start) + 1L;
            return (int) Math.max(1L, diffDays);
        }

        long entityDays = item.getRentalDays();
        return (int) Math.max(1L, entityDays);
    }

    private void placeOrder() {
        if (checkoutItems.isEmpty()) {
            Snackbar.make(findViewById(R.id.main), R.string.checkout_empty_cart, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!validateBillingDetails()) {
            return;
        }

        authenticateWithBiometrics();
    }

    private void authenticateWithBiometrics() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(CheckoutActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Snackbar.make(findViewById(R.id.main), getString(R.string.checkout_auth_error, errString),
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        proceedWithOrder();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Snackbar.make(findViewById(R.id.main), R.string.checkout_auth_failed, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.checkout_auth_title))
                .setSubtitle(getString(R.string.checkout_auth_subtitle))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
                        | androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void proceedWithOrder() {
        if (radioCheckoutCash != null && radioCheckoutCash.isChecked()) {
            placeCashOrder();
            return;
        }

        startStripePayment();
    }

    private void placeCashOrder() {
        if (paymentInProgress) {
            return;
        }

        paymentInProgress = true;
        btnPlaceOrder.setEnabled(false);

        String selectedHub = safeText(actvCollectingHub);
        long startDateUtc = resolveOrderStartDate();
        long endDateUtc = resolveOrderEndDate();

        stripeApiHelper.createCashOrder(selectedHub, startDateUtc, endDateUtc, new StripeApiHelper.StripeCallback<>() {
            @Override
            public void onSuccess(CashOrderResponse result) {
                runOnUiThread(() -> {
                    paymentInProgress = false;
                    checkoutItems.clear();
                    btnPlaceOrder.setEnabled(false);

                    String invoiceCode = result != null ? result.getInvoiceCode() : null;
                    openCheckoutSuccessScreen(invoiceCode);
                });
                    cartHelper.clearCart(); // Clear local cart after order
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    paymentInProgress = false;
                    btnPlaceOrder.setEnabled(!checkoutItems.isEmpty());
                    Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void placeCardOrder() {
        if (paymentInProgress) {
            return;
        }

        paymentInProgress = true;
        btnPlaceOrder.setEnabled(false);

        String selectedHub = safeText(actvCollectingHub);
        long startDateUtc = resolveOrderStartDate();
        long endDateUtc = resolveOrderEndDate();

        stripeApiHelper.createCardOrder(selectedHub, startDateUtc, endDateUtc, new StripeApiHelper.StripeCallback<>() {
            @Override
            public void onSuccess(CashOrderResponse result) {
                runOnUiThread(() -> {
                    paymentInProgress = false;
                    checkoutItems.clear();
                    btnPlaceOrder.setEnabled(false);

                    String invoiceCode = result != null ? result.getInvoiceCode() : null;
                    openCheckoutSuccessScreen(invoiceCode);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    paymentInProgress = false;
                    btnPlaceOrder.setEnabled(!checkoutItems.isEmpty());
                    Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startStripePayment() {
        if (paymentInProgress) {
            return;
        }

        paymentInProgress = true;
        btnPlaceOrder.setEnabled(false);

        stripeApiHelper.createPaymentIntent(new StripeApiHelper.StripeCallback<>() {
            @Override
            public void onSuccess(StripePaymentIntentResponse result) {
                runOnUiThread(() -> presentPaymentSheet(result));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    paymentInProgress = false;
                    btnPlaceOrder.setEnabled(true);
                    Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void presentPaymentSheet(StripePaymentIntentResponse paymentIntentResponse) {
        String publishableKey = paymentIntentResponse != null ? paymentIntentResponse.getPublishableKey() : null;
        String clientSecret = paymentIntentResponse != null ? paymentIntentResponse.getClientSecret() : null;

        if (TextUtils.isEmpty(publishableKey) || TextUtils.isEmpty(clientSecret)) {
            paymentInProgress = false;
            btnPlaceOrder.setEnabled(true);
            Snackbar.make(findViewById(R.id.main), R.string.checkout_payment_init_failed, Snackbar.LENGTH_LONG).show();
            return;
        }

        PaymentConfiguration.init(this, publishableKey);

        PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails.Builder()
                .name(safeText(etBillingName))
                .email(safeText(etBillingEmail))
                .phone(safeText(etBillingPhone))
                .address(new PaymentSheet.Address.Builder()
                        .line1(safeText(etBillingAddress))
                        .build())
                .build();

        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("EcoTrekk")
                .defaultBillingDetails(billingDetails)
                .build();

        paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        paymentInProgress = false;
        btnPlaceOrder.setEnabled(!checkoutItems.isEmpty());

        if (result instanceof PaymentSheetResult.Completed) {
            placeCardOrder();
            return;
        }

        if (result instanceof PaymentSheetResult.Canceled) {
            Snackbar.make(findViewById(R.id.main), R.string.checkout_payment_cancelled, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (result instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) result;
            String message = failedResult.getError() != null && failedResult.getError().getLocalizedMessage() != null
                    ? failedResult.getError().getLocalizedMessage()
                    : getString(R.string.checkout_payment_error_generic);
            Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean validateBillingDetails() {
        boolean isValid = true;

        String selectedHub = safeText(actvCollectingHub);
        String billingName = safeText(etBillingName);
        String billingEmail = safeText(etBillingEmail);
        String billingPhone = safeText(etBillingPhone);
        String billingAddress = safeText(etBillingAddress);

        inputLayoutCollectingHub.setError(null);
        inputLayoutBillingName.setError(null);
        inputLayoutBillingEmail.setError(null);
        inputLayoutBillingPhone.setError(null);
        inputLayoutBillingAddress.setError(null);

        if (TextUtils.isEmpty(selectedHub)) {
            inputLayoutCollectingHub.setError(getString(R.string.checkout_hub_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(billingName)) {
            inputLayoutBillingName.setError(getString(R.string.checkout_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(billingEmail)) {
            inputLayoutBillingEmail.setError(getString(R.string.checkout_field_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(billingEmail).matches()) {
            inputLayoutBillingEmail.setError(getString(R.string.checkout_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(billingPhone)) {
            inputLayoutBillingPhone.setError(getString(R.string.checkout_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(billingAddress)) {
            inputLayoutBillingAddress.setError(getString(R.string.checkout_field_required));
            isValid = false;
        }

        return isValid;
    }

    private long resolveOrderStartDate() {
        if (selectedRangeStartUtc > 0) {
            return selectedRangeStartUtc;
        }
        if (!checkoutItems.isEmpty()) {
            return checkoutItems.get(0).getStartDate();
        }
        return System.currentTimeMillis();
    }

    private long resolveOrderEndDate() {
        if (selectedRangeEndUtc > 0) {
            return selectedRangeEndUtc;
        }
        if (!checkoutItems.isEmpty()) {
            return checkoutItems.get(0).getEndDate();
        }
        return System.currentTimeMillis();
    }

    private String safeText(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "LKR %,.2f", amount);
    }

    private void openCheckoutSuccessScreen(String invoiceCode) {
        Intent intent = new Intent(this, CheckoutSuccessActivity.class);
        if (!TextUtils.isEmpty(invoiceCode)) {
            intent.putExtra(CheckoutSuccessActivity.EXTRA_INVOICE_CODE, invoiceCode);
        }
        startActivity(intent);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
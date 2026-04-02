package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.akigen.ecotrekk.R;
import com.google.android.material.button.MaterialButton;

public class CheckoutSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_CODE = "extra_invoice_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_success);

        TextView txtInvoiceCode = findViewById(R.id.txtCheckoutSuccessInvoiceCode);
        MaterialButton btnContinueShopping = findViewById(R.id.btnCheckoutSuccessContinueShopping);
        MaterialButton btnDone = findViewById(R.id.btnCheckoutSuccessDone);

        String invoiceCode = getIntent().getStringExtra(EXTRA_INVOICE_CODE);
        if (TextUtils.isEmpty(invoiceCode)) {
            txtInvoiceCode.setText(getString(R.string.checkout_success_no_invoice));
        } else {
            txtInvoiceCode.setText(getString(R.string.checkout_success_invoice_format, invoiceCode));
        }

        btnContinueShopping.setOnClickListener(v -> openHome());
        btnDone.setOnClickListener(v -> openOrderHistory());
    }

    private void openHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void openOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivitySignupBinding;
import com.akigen.ecotrekk.dto.CustomerAuthResponse;
import com.akigen.ecotrekk.dto.CustomerRegisterRequest;
import com.akigen.ecotrekk.dto.CustomerRegisterResponse;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.manager.SessionManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Customer registration — driven entirely by the Spring Boot backend.
 *
 * Flow:
 * 1. User fills in name / email / phone / password.
 * 2. POST /api/auth/customer/register → backend creates Firebase Auth user +
 * MySQL record.
 * 3. Backend returns a Firebase custom token + customer profile.
 * 4. App calls FirebaseAuth.signInWithCustomToken() to establish a local
 * Firebase session.
 * 5. Save customer profile to SessionManager → navigate to HomeActivity.
 *
 * No Firebase client-side createUser. No Firestore.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private ActivitySignupBinding binding;
    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        attachErrorResetWatcher(binding.signupActivityNameInput);
        attachErrorResetWatcher(binding.signupActivityEmailInput);
        attachErrorResetWatcher(binding.signupActivityPhoneInput);
        attachErrorResetWatcher(binding.signupActivityPasswordInput);
        attachErrorResetWatcher(binding.signupActivityConfirmPasswordInput);

        binding.closeErrorBanner.setOnClickListener(v -> hideError());

        binding.signupActivityLoginBtn.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        binding.signupActivityBackButton.setOnClickListener(view -> finish());

        binding.signupActivitySignupBtn.setOnClickListener(view -> {
            hideError();
            clearErrors();

            String name = binding.signupActivityNameInput.getEditText().getText().toString().trim();
            String phoneNumber = binding.signupActivityPhoneInput.getEditText().getText().toString().trim();
            String email = binding.signupActivityEmailInput.getEditText().getText().toString().trim();
            String password = binding.signupActivityPasswordInput.getEditText().getText().toString();
            String confirmPassword = binding.signupActivityConfirmPasswordInput.getEditText().getText().toString();

            if (!validateInputs(name, phoneNumber, email, password, confirmPassword))
                return;

            setLoading(true);

            // Step 1: call the backend to create the account
            callBackendRegister(name, email, password, phoneNumber);
        });
    }

    // ── Backend registration ──────────────────────────────────────────────────

    private void callBackendRegister(String name, String email, String password, String phone) {
        CustomerRegisterRequest req = new CustomerRegisterRequest(name, email, password, phone);
        RetrofitClient.getInstance().getApiService()
                .customerRegister(req)
                .enqueue(new Callback<CustomerRegisterResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CustomerRegisterResponse> call,
                            @NonNull Response<CustomerRegisterResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            signInWithCustomToken(response.body());
                        } else {
                            int code = response.code();
                            String msg = code == 409
                                    ? "An account with this email already exists."
                                    : "Registration failed. Please try again.";
                            Log.e(TAG, "Backend register failed: " + code);
                            showError(msg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CustomerRegisterResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error during registration", t);
                        showError("Network error. Check your connection.");
                    }
                });
    }

    // Step 2: sign in with the Firebase custom token returned by the backend
    private void signInWithCustomToken(CustomerRegisterResponse registerResponse) {
        firebaseAuth.signInWithCustomToken(registerResponse.getCustomToken())
                .addOnSuccessListener(authResult -> {
                    // Step 3: save customer profile to SessionManager
                    CustomerAuthResponse profile = new CustomerAuthResponse(
                            registerResponse.getCustomerId(),
                            registerResponse.getFirebaseUid(),
                            registerResponse.getFullName(),
                            registerResponse.getEmail(),
                            registerResponse.getPhone(),
                            registerResponse.getProfilePictureUrl());
                    sessionManager.saveSession(profile);
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithCustomToken failed", e);
                    // Account was created — ask user to log in
                    showError("Account created! Please log in.");
                    navigateToLogin();
                });
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void navigateToHome() {
        runOnUiThread(() -> {
            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToLogin() {
        runOnUiThread(() -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showError(String message) {
        runOnUiThread(() -> {
            binding.errorBannerText.setText(message);
            binding.errorBanner.setVisibility(View.VISIBLE);
            setLoading(false);
        });
    }

    private void hideError() {
        binding.errorBanner.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        binding.signupActivitySignupBtn.setEnabled(!loading);
        binding.signupActivityLoginBtn.setEnabled(!loading);
        binding.signupActivitySignupBtn.setText(loading ? R.string.creating_account : R.string.signup_btn_txt);
    }

    private void clearErrors() {
        binding.signupActivityNameInput.setError(null);
        binding.signupActivityEmailInput.setError(null);
        binding.signupActivityPhoneInput.setError(null);
        binding.signupActivityPasswordInput.setError(null);
        binding.signupActivityConfirmPasswordInput.setError(null);
        binding.signupActivityTermsCheckbox.setError(null);
    }

    private void attachErrorResetWatcher(TextInputLayout inputLayout) {
        if (inputLayout.getEditText() == null)
            return;
        inputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputLayout.getError() != null) {
                    inputLayout.setError(null);
                    inputLayout.setErrorEnabled(false);
                }
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean validateInputs(String name, String phone, String email,
            String password, String confirmPassword) {
        boolean valid = true;
        if (name.isEmpty()) {
            binding.signupActivityNameInput.setError("Name is required");
            binding.signupActivityNameInput.requestFocus();
            valid = false;
        }
        if (phone.isEmpty()) {
            binding.signupActivityPhoneInput.setError("Phone number is required");
            binding.signupActivityPhoneInput.requestFocus();
            valid = false;
        } else if (phone.length() < 10) {
            binding.signupActivityPhoneInput.setError("Phone number must be at least 10 digits");
            binding.signupActivityPhoneInput.requestFocus();
            valid = false;
        }
        if (email.isEmpty()) {
            binding.signupActivityEmailInput.setError("Email is required");
            binding.signupActivityEmailInput.requestFocus();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupActivityEmailInput.setError("Enter a valid email address");
            binding.signupActivityEmailInput.requestFocus();
            valid = false;
        }
        if (password.isEmpty()) {
            binding.signupActivityPasswordInput.setError("Password is required");
            binding.signupActivityPasswordInput.requestFocus();
            valid = false;
        } else if (password.length() < 6) {
            binding.signupActivityPasswordInput.setError("Password must be at least 6 characters");
            binding.signupActivityPasswordInput.requestFocus();
            valid = false;
        }
        if (confirmPassword.isEmpty()) {
            binding.signupActivityConfirmPasswordInput.setError("Please confirm your password");
            binding.signupActivityConfirmPasswordInput.requestFocus();
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.signupActivityConfirmPasswordInput.setError("Passwords do not match");
            binding.signupActivityConfirmPasswordInput.requestFocus();
            valid = false;
        }
        if (!binding.signupActivityTermsCheckbox.isChecked()) {
            binding.signupActivityTermsCheckbox.setError("You must agree to the terms and conditions");
            binding.signupActivityTermsCheckbox.requestFocus();
            valid = false;
        }
        return valid;
    }
}

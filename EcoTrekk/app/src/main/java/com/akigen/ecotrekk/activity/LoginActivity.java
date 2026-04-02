package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivityLoginBinding;
import com.akigen.ecotrekk.api.ApiService;
import com.akigen.ecotrekk.dto.CustomerAuthRequest;
import com.akigen.ecotrekk.dto.CustomerAuthResponse;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.database.UserSessionManager;
import com.akigen.ecotrekk.manager.FcmManager;
import com.akigen.ecotrekk.manager.SessionManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;
    private UserSessionManager userSessionManager;  // SQLite session manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        userSessionManager = UserSessionManager.getInstance(this);  // Initialize SQLite session manager

        attachErrorResetWatcher(binding.loginActivityEmailInput);
        attachErrorResetWatcher(binding.loginActivityPasswordInput);

        binding.closeErrorBanner.setOnClickListener(v -> hideError());

        binding.loginActivitySignupBtn.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        binding.loginActivityBackButton.setOnClickListener(view -> finish());

        binding.loginActivityLoginBtn.setOnClickListener(view -> {
            hideError();
            binding.loginActivityEmailInput.setError(null);
            binding.loginActivityPasswordInput.setError(null);

            String email = binding.loginActivityEmailInput.getEditText().getText().toString().trim();
            String password = binding.loginActivityPasswordInput.getEditText().getText().toString();

            if (!validateInputs(email, password))
                return;

            setLoading(true);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                fetchBackendSession(user);
                            } else {
                                showError("Login failed. Please try again.");
                            }
                        } else {
                            Log.e(TAG, "Firebase sign-in failed", task.getException());
                            showError("Invalid email or password.");
                        }
                    });
        });

        // Forgot Password logic
        binding.loginActivityForgotPasswordBtn.setOnClickListener(view -> showForgotPasswordDialog());
    }
    // ── Forgot Password ──────────────────────────────────────────────────────
    private void showForgotPasswordDialog() {
        // Create a container with padding
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, 0);

        // Description text
        android.widget.TextView description = new android.widget.TextView(this);
        description.setText("Enter your email address to receive a password reset link.");
        description.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        description.setPadding(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
        container.addView(description);

        // TextInputLayout for Material floating label and error
        com.google.android.material.textfield.TextInputLayout textInputLayout = new com.google.android.material.textfield.TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        textInputLayout.setHint("Email");
        textInputLayout.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxCornerRadii(16, 16, 16, 16);

        final EditText emailInput = new com.google.android.material.textfield.TextInputEditText(this);
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setSingleLine(true);
        textInputLayout.addView(emailInput);
        container.addView(textInputLayout);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Reset Password")
                .setView(container)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Send", null); // We'll override this below

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = emailInput.getText().toString().trim();
                if (email.isEmpty()) {
                    textInputLayout.setError("Email is required");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    textInputLayout.setError("Enter a valid email address");
                } else {
                    textInputLayout.setError(null);
                    sendPasswordResetEmail(email);
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        setLoading(true);
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        showError("Password reset email sent. Check your inbox.");
                    } else {
                        showError("Failed to send reset email. Try again.");
                    }
                });
    }

    // ── Step 2: get Firebase ID token and call the backend ────────────────────

    private void fetchBackendSession(FirebaseUser firebaseUser) {
        firebaseUser.getIdToken(true)
                .addOnSuccessListener(result -> {
                    String idToken = result.getToken();
                    if (idToken == null) {
                        showError("Authentication error. Please try again.");
                        return;
                    }
                    callBackendLogin(idToken);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get Firebase ID token", e);
                    showError("Authentication error. Please try again.");
                });
    }

    private void callBackendLogin(String idToken) {
        ApiService api = RetrofitClient.getInstance().getApiService();
        api.customerFirebaseLogin(new CustomerAuthRequest(idToken))
                .enqueue(new Callback<CustomerAuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CustomerAuthResponse> call,
                            @NonNull Response<CustomerAuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            sessionManager.saveSession(response.body());
                            
                            // Also save to SQLite database
                            CustomerAuthResponse authResponse = response.body();
                            userSessionManager.loginUser(
                                String.valueOf(authResponse.getCustomerId()),
                                authResponse.getEmail(),
                                authResponse.getFullName(),
                                authResponse.getPhone() != null ? authResponse.getPhone() : "",
                                idToken  // Store Firebase ID token
                            );

                            // Non-blocking token registration. Login should continue even if this fails.
                            FcmManager.syncTokenToBackend(LoginActivity.this);
                            
                            navigateToHome();
                        } else {
                            Log.e(TAG, "Backend login failed: " + response.code());
                            showError("Login failed. Please try again later.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CustomerAuthResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error during backend login", t);
                        showError("Network error. Check your connection.");
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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
        binding.loginActivityLoginBtn.setEnabled(!loading);
        binding.loginActivitySignupBtn.setEnabled(!loading);
        binding.loginActivityLoginBtn.setText(loading ? R.string.logging_in : R.string.log_in);
    }

    private boolean validateInputs(String email, String password) {
        boolean valid = true;
        if (email.isEmpty()) {
            binding.loginActivityEmailInput.setError("Email is required");
            binding.loginActivityEmailInput.requestFocus();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginActivityEmailInput.setError("Enter a valid email address");
            binding.loginActivityEmailInput.requestFocus();
            valid = false;
        }
        if (password.isEmpty()) {
            binding.loginActivityPasswordInput.setError("Password is required");
            binding.loginActivityPasswordInput.requestFocus();
            valid = false;
        } else if (password.length() < 6) {
            binding.loginActivityPasswordInput.setError("Password must be at least 6 characters");
            binding.loginActivityPasswordInput.requestFocus();
            valid = false;
        }
        return valid;
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
}

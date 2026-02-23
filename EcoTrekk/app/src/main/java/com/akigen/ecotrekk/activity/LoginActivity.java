package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        // Clear error + reclaim spacing as soon as the user starts correcting each
        // field
        attachErrorResetWatcher(binding.loginActivityEmailInput);
        attachErrorResetWatcher(binding.loginActivityPasswordInput);

        binding.loginActivitySignupBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        binding.loginActivityBackButton.setOnClickListener(view -> {
            finish();
        });

        binding.loginActivityLoginBtn.setOnClickListener(view -> {
            binding.loginActivityEmailInput.setError(null);
            binding.loginActivityPasswordInput.setError(null);

            binding.loginActivitySignupBtn.setEnabled(false);
            binding.loginActivityLoginBtn.setEnabled(false);
            binding.loginActivityLoginBtn.setText("Logging in...");

            String email = binding.loginActivityEmailInput.getEditText().getText().toString();
            String password = binding.loginActivityPasswordInput.getEditText().getText().toString();

            if (validateInputs(email, password)) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });
            }

        });

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            enableButtons();
        }
    }

    private void enableButtons() {
        binding.loginActivitySignupBtn.setEnabled(true);
        binding.loginActivityLoginBtn.setEnabled(true);
        binding.loginActivityLoginBtn.setText("Log In");
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            binding.loginActivityEmailInput.setError("Email is required");
            binding.loginActivityEmailInput.requestFocus();
            isValid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginActivityEmailInput.setError("Enter a valid email address");
            binding.loginActivityEmailInput.requestFocus();
            isValid = false;
        }
        if (password.isEmpty()) {
            binding.loginActivityPasswordInput.setError("Password is required");
            binding.loginActivityPasswordInput.requestFocus();
            isValid = false;
        }
        if (password.length() < 6) {
            binding.loginActivityPasswordInput.setError("Password must be at least 6 characters");
            binding.loginActivityPasswordInput.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    // Attaches a TextWatcher that clears the error AND collapses the error space
    // when the user types.
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
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
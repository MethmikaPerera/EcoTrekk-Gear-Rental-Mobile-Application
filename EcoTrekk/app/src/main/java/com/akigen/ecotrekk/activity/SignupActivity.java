package com.akigen.ecotrekk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akigen.ecotrekk.databinding.ActivitySignupBinding;
import com.akigen.ecotrekk.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Clear error + reclaim spacing as soon as the user starts correcting each field
        attachErrorResetWatcher(binding.signupActivityNameInput);
        attachErrorResetWatcher(binding.signupActivityEmailInput);
        attachErrorResetWatcher(binding.signupActivityPhoneInput);
        attachErrorResetWatcher(binding.signupActivityPasswordInput);
        attachErrorResetWatcher(binding.signupActivityConfirmPasswordInput);

        binding.signupActivityLoginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.signupActivityBackButton.setOnClickListener(view -> {
            finish();
        });

        binding.signupActivitySignupBtn.setOnClickListener(view -> {
            binding.signupActivityNameInput.setError(null);
            binding.signupActivityEmailInput.setError(null);
            binding.signupActivityPhoneInput.setError(null);
            binding.signupActivityPasswordInput.setError(null);
            binding.signupActivityConfirmPasswordInput.setError(null);
            binding.signupActivityTermsCheckbox.setError(null);

            binding.signupActivityLoginBtn.setEnabled(false);
            binding.signupActivitySignupBtn.setEnabled(false);
            binding.signupActivitySignupBtn.setText("Creating account...");

            String name = binding.signupActivityNameInput.getEditText().getText().toString();
            String phoneNumber = binding.signupActivityPhoneInput.getEditText().getText().toString();
            String email = binding.signupActivityEmailInput.getEditText().getText().toString();
            String password = binding.signupActivityPasswordInput.getEditText().getText().toString();
            String confirmPassword = binding.signupActivityConfirmPasswordInput.getEditText().getText().toString();

            String profilePicture = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";

            if (validateInputs(name, phoneNumber, email, password, confirmPassword)) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = task.getResult().getUser().getUid();
                                User user = User.builder()
                                        .uid(uid)
                                        .uname(name)
                                        .email(email)
                                        .phone(phoneNumber)
                                        .profilePicture(profilePicture)
                                        .build();

                                firebaseFirestore.collection("users")
                                        .document(uid)
                                        .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                enableButtons();
                                                binding.signupActivitySignupBtn.setError("Failed to register");
                                                binding.signupActivitySignupBtn.requestFocus();
                                            }
                                        });

                            } else {
                                binding.signupActivitySignupBtn.setError("Failed to register");
                                binding.signupActivitySignupBtn.requestFocus();
                            }
                        });
            }
        });
    }

    private void enableButtons() {
        binding.signupActivityLoginBtn.setEnabled(true);
        binding.signupActivitySignupBtn.setEnabled(true);
        binding.signupActivitySignupBtn.setText("Sign Up");
    }

    //Attaches a TextWatcher that clears the error AND collapses the error space when the user types.
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

    private boolean validateInputs(String name, String phoneNumber, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (name.isEmpty()) {
            binding.signupActivityNameInput.setError("Name is required");
            binding.signupActivityNameInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (phoneNumber.isEmpty()) {
            binding.signupActivityPhoneInput.setError("Phone number is required");
            binding.signupActivityPhoneInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (phoneNumber.length() < 10) {
            binding.signupActivityPhoneInput.setError("Phone number must be at least 10 digits");
            binding.signupActivityPhoneInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (email.isEmpty()) {
            binding.signupActivityEmailInput.setError("Email is required");
            binding.signupActivityEmailInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupActivityEmailInput.setError("Enter a valid email address");
            binding.signupActivityEmailInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (password.isEmpty()) {
            binding.signupActivityPasswordInput.setError("Password is required");
            binding.signupActivityPasswordInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (password.length() < 6) {
            binding.signupActivityPasswordInput.setError("Password must be at least 6 characters");
            binding.signupActivityPasswordInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (confirmPassword.isEmpty()) {
            binding.signupActivityConfirmPasswordInput.setError("Confirm password is required");
            binding.signupActivityConfirmPasswordInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (!password.equals(confirmPassword)) {
            binding.signupActivityConfirmPasswordInput.setError("Passwords do not match");
            binding.signupActivityConfirmPasswordInput.requestFocus();
            enableButtons();
            isValid = false;
        }
        if (!binding.signupActivityTermsCheckbox.isChecked()) {
            binding.signupActivityTermsCheckbox.setError("You must agree to the terms and conditions");
            binding.signupActivityTermsCheckbox.requestFocus();
            enableButtons();
            isValid = false;
        }
        return isValid;
    }
}
package com.akigen.ecotrekk.activity;

import android.content.res.ColorStateList;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.api.ApiService;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.databinding.ActivityPersonalInformationBinding;
import com.akigen.ecotrekk.dto.CustomerAuthRequest;
import com.akigen.ecotrekk.dto.CustomerAuthResponse;
import com.akigen.ecotrekk.dto.CustomerProfileUpdateRequest;
import com.akigen.ecotrekk.manager.SessionManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalInformationActivity extends AppCompatActivity {

    private static final String TAG = "PersonalInfoActivity";

    private ActivityPersonalInformationBinding binding;
    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;
    private ApiService apiService;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPersonalInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getInstance().getApiService();

        binding.personalInfoBackButton.setOnClickListener(v -> {
            finish();
        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);
        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri croppedUri = UCrop.getOutput(result.getData());
                        if (croppedUri != null) {
                            uploadProfileImageAfterToken(croppedUri);
                        } else {
                            Toast.makeText(this, "Image crop failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (result.getData() != null) {
                        Throwable cropError = UCrop.getError(result.getData());
                        if (cropError != null) {
                            Log.e(TAG, "Crop error", cropError);
                        }
                    }
                });
        binding.personalInfoEditPhotoBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.personalInfoUpdateProfileBtn.setOnClickListener(v -> updateProfileDetails());
        binding.personalInfoUpdatePasswordBtn.setOnClickListener(v -> changePassword());

        // Load quick local snapshot first so the UI is not empty.
        bindFromSession();
        // Refresh with the latest profile state from backend.
        fetchProfileFromBackend();
    }

    private void bindFromSession() {
        binding.personalInfoNameEditText.setText(sessionManager.getFullName());
        binding.personalInfoEmailEditText.setText(sessionManager.getEmail());
        binding.personalInfoPhoneEditText.setText(sessionManager.getPhone());

        renderProfileImage(sessionManager.getPhotoUrl());
    }

    private void fetchProfileFromBackend() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.getIdToken(true)
                .addOnSuccessListener(tokenResult -> {
                    String idToken = tokenResult.getToken();
                    if (TextUtils.isEmpty(idToken)) {
                        Toast.makeText(this, "Unable to refresh profile.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    requestProfile(idToken);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Token refresh failed.", Toast.LENGTH_SHORT).show());
    }

    private void requestProfile(String idToken) {
        apiService.customerFirebaseLogin(new CustomerAuthRequest(idToken))
                .enqueue(new Callback<CustomerAuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CustomerAuthResponse> call,
                                           @NonNull Response<CustomerAuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CustomerAuthResponse profile = response.body();
                            sessionManager.saveSession(profile);
                            bindFromProfile(profile);
                        } else {
                            Toast.makeText(PersonalInformationActivity.this,
                                    "Could not fetch profile.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CustomerAuthResponse> call, @NonNull Throwable t) {
                        Toast.makeText(PersonalInformationActivity.this,
                                "Network error while fetching profile.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindFromProfile(CustomerAuthResponse profile) {
        if (!TextUtils.isEmpty(profile.getFullName())) {
            binding.personalInfoNameEditText.setText(profile.getFullName());
        }
        if (!TextUtils.isEmpty(profile.getEmail())) {
            binding.personalInfoEmailEditText.setText(profile.getEmail());
        }
        if (!TextUtils.isEmpty(profile.getPhone())) {
            binding.personalInfoPhoneEditText.setText(profile.getPhone());
        }

        renderProfileImage(profile.getProfilePictureUrl());
    }

    private void renderProfileImage(String photoUrl) {
        if (TextUtils.isEmpty(photoUrl)) {
            showFallbackAvatar();
            return;
        }

        // Real DB image: remove icon tint/padding so Glide image appears as-is.
        binding.profileAvatar.setImageTintList(null);
        binding.profileAvatar.setColorFilter(null);
        binding.profileAvatar.setPadding(0, 0, 0, 0);

        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.person_24px)
                .error(R.drawable.person_24px)
                .into(binding.profileAvatar);
    }

    private void showFallbackAvatar() {
        Log.d(TAG, "Profile image URL missing, using fallback icon");
        binding.profileAvatar.setImageResource(R.drawable.person_24px);
        binding.profileAvatar.setPadding(15, 15, 15, 15);
        binding.profileAvatar.setImageTintList(
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_theme_primary)));
    }

    private void onImageSelected(Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        launchCrop(imageUri);
    }

    private void launchCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(),
                "profile_crop_" + System.currentTimeMillis() + ".jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(false);
        options.setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);

        Intent cropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
                .withOptions(options)
                .getIntent(this);

        cropImageLauncher.launch(cropIntent);
    }

    private void uploadProfileImageAfterToken(Uri imageUri) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.getIdToken(true)
                .addOnSuccessListener(tokenResult -> {
                    String idToken = tokenResult.getToken();
                    if (TextUtils.isEmpty(idToken)) {
                        Toast.makeText(this, "Authentication token missing.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uploadProfileImage(imageUri, idToken);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to refresh Firebase token for image upload", e);
                    Toast.makeText(this, "Token refresh failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadProfileImage(Uri imageUri, String idToken) {
        byte[] imageBytes;
        try {
            imageBytes = readImageBytes(imageUri);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read selected image", e);
            Toast.makeText(this, "Unable to read selected image.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mimeType = getContentResolver().getType(imageUri);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "image/*";
        }

        RequestBody imageBody = RequestBody.create(imageBytes, MediaType.parse(mimeType));
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "profile.jpg", imageBody);
        RequestBody idTokenPart = RequestBody.create(idToken, MediaType.parse("text/plain"));

        binding.personalInfoEditPhotoBtn.setEnabled(false);

        apiService.updateCustomerProfileImage(imagePart, idTokenPart)
                .enqueue(new Callback<CustomerAuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CustomerAuthResponse> call,
                                           @NonNull Response<CustomerAuthResponse> response) {
                        binding.personalInfoEditPhotoBtn.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            CustomerAuthResponse profile = response.body();
                            sessionManager.saveSession(profile);
                            bindFromProfile(profile);
                            Toast.makeText(PersonalInformationActivity.this,
                                    "Profile image updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Profile image upload failed: HTTP " + response.code());
                            Toast.makeText(PersonalInformationActivity.this,
                                    "Failed to update profile image.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CustomerAuthResponse> call, @NonNull Throwable t) {
                        binding.personalInfoEditPhotoBtn.setEnabled(true);
                        Log.e(TAG, "Network error while uploading profile image", t);
                        Toast.makeText(PersonalInformationActivity.this,
                                "Network error while uploading image.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfileDetails() {
        String fullName = binding.personalInfoNameEditText.getText() != null
                ? binding.personalInfoNameEditText.getText().toString().trim() : "";
        String email = binding.personalInfoEmailEditText.getText() != null
                ? binding.personalInfoEmailEditText.getText().toString().trim() : "";
        String phone = binding.personalInfoPhoneEditText.getText() != null
                ? binding.personalInfoPhoneEditText.getText().toString().trim() : "";

        if (!validateProfileInputs(fullName, email)) {
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.personalInfoUpdateProfileBtn.setEnabled(false);

        currentUser.getIdToken(true)
                .addOnSuccessListener(tokenResult -> {
                    String idToken = tokenResult.getToken();
                    if (TextUtils.isEmpty(idToken)) {
                        binding.personalInfoUpdateProfileBtn.setEnabled(true);
                        Toast.makeText(this, "Authentication token missing.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitProfileUpdate(idToken, fullName, email, phone);
                })
                .addOnFailureListener(e -> {
                    binding.personalInfoUpdateProfileBtn.setEnabled(true);
                    Log.e(TAG, "Failed to refresh token for profile update", e);
                    Toast.makeText(this, "Token refresh failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitProfileUpdate(String idToken, String fullName, String email, String phone) {
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest(idToken, fullName, email, phone);

        apiService.updateCustomerProfile(request).enqueue(new Callback<CustomerAuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<CustomerAuthResponse> call,
                                   @NonNull Response<CustomerAuthResponse> response) {
                binding.personalInfoUpdateProfileBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    CustomerAuthResponse profile = response.body();
                    sessionManager.saveSession(profile);
                    bindFromProfile(profile);
                    Toast.makeText(PersonalInformationActivity.this,
                            "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 409) {
                    Toast.makeText(PersonalInformationActivity.this,
                            "Email is already in use.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Profile update failed: HTTP " + response.code());
                    Toast.makeText(PersonalInformationActivity.this,
                            "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CustomerAuthResponse> call, @NonNull Throwable t) {
                binding.personalInfoUpdateProfileBtn.setEnabled(true);
                Log.e(TAG, "Network error while updating profile", t);
                Toast.makeText(PersonalInformationActivity.this,
                        "Network error while updating profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePassword() {
        String currentPassword = binding.personalInfoCurrentPasswordEditText.getText() != null
                ? binding.personalInfoCurrentPasswordEditText.getText().toString() : "";
        String newPassword = binding.personalInfoNewPasswordEditText.getText() != null
                ? binding.personalInfoNewPasswordEditText.getText().toString() : "";
        String confirmPassword = binding.personalInfoConfirmPasswordEditText.getText() != null
                ? binding.personalInfoConfirmPasswordEditText.getText().toString() : "";

        if (!validatePasswordInputs(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || TextUtils.isEmpty(currentUser.getEmail())) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.personalInfoUpdatePasswordBtn.setEnabled(false);

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(unused -> currentUser.updatePassword(newPassword)
                        .addOnSuccessListener(unused2 -> {
                            binding.personalInfoUpdatePasswordBtn.setEnabled(true);
                            clearPasswordFields();
                            Toast.makeText(PersonalInformationActivity.this,
                                    "Password updated successfully.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            binding.personalInfoUpdatePasswordBtn.setEnabled(true);
                            Log.e(TAG, "Password update failed", e);
                            Toast.makeText(PersonalInformationActivity.this,
                                    "Failed to update password.", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    binding.personalInfoUpdatePasswordBtn.setEnabled(true);
                    Log.e(TAG, "Re-authentication failed", e);
                    binding.personalInfoCurrentPasswordInput.setError("Current password is incorrect");
                    binding.personalInfoCurrentPasswordInput.requestFocus();
                });
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        boolean valid = true;

        binding.personalInfoCurrentPasswordInput.setError(null);
        binding.personalInfoNewPasswordInput.setError(null);
        binding.personalInfoConfirmPasswordInput.setError(null);

        if (TextUtils.isEmpty(currentPassword)) {
            binding.personalInfoCurrentPasswordInput.setError("Current password is required");
            binding.personalInfoCurrentPasswordInput.requestFocus();
            valid = false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            binding.personalInfoNewPasswordInput.setError("New password is required");
            if (valid) {
                binding.personalInfoNewPasswordInput.requestFocus();
            }
            valid = false;
        } else if (newPassword.length() < 6) {
            binding.personalInfoNewPasswordInput.setError("New password must be at least 6 characters");
            if (valid) {
                binding.personalInfoNewPasswordInput.requestFocus();
            }
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.personalInfoConfirmPasswordInput.setError("Please confirm your new password");
            if (valid) {
                binding.personalInfoConfirmPasswordInput.requestFocus();
            }
            valid = false;
        } else if (!TextUtils.equals(newPassword, confirmPassword)) {
            binding.personalInfoConfirmPasswordInput.setError("Passwords do not match");
            if (valid) {
                binding.personalInfoConfirmPasswordInput.requestFocus();
            }
            valid = false;
        }

        if (TextUtils.equals(currentPassword, newPassword) && !TextUtils.isEmpty(newPassword)) {
            binding.personalInfoNewPasswordInput.setError("New password must be different");
            if (valid) {
                binding.personalInfoNewPasswordInput.requestFocus();
            }
            valid = false;
        }

        return valid;
    }

    private void clearPasswordFields() {
        binding.personalInfoCurrentPasswordEditText.setText("");
        binding.personalInfoNewPasswordEditText.setText("");
        binding.personalInfoConfirmPasswordEditText.setText("");
        binding.personalInfoCurrentPasswordInput.setError(null);
        binding.personalInfoNewPasswordInput.setError(null);
        binding.personalInfoConfirmPasswordInput.setError(null);
    }

    private boolean validateProfileInputs(String fullName, String email) {
        if (TextUtils.isEmpty(fullName)) {
            binding.personalInfoNameInput.setError("Name is required");
            binding.personalInfoNameInput.requestFocus();
            return false;
        }
        binding.personalInfoNameInput.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.personalInfoEmailInput.setError("Email is required");
            binding.personalInfoEmailInput.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.personalInfoEmailInput.setError("Enter a valid email");
            binding.personalInfoEmailInput.requestFocus();
            return false;
        }
        binding.personalInfoEmailInput.setError(null);
        return true;
    }

    private byte[] readImageBytes(Uri imageUri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {

            if (inputStream == null) {
                throw new IOException("InputStream is null for selected image URI");
            }

            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, read);
            }
            return byteBuffer.toByteArray();
        }
    }
}
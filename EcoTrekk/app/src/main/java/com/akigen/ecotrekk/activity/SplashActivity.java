package com.akigen.ecotrekk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivitySplashBinding;
import com.akigen.ecotrekk.api.ApiService;
import com.akigen.ecotrekk.dto.CustomerAuthRequest;
import com.akigen.ecotrekk.dto.CustomerAuthResponse;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.manager.SessionManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private ActivitySplashBinding binding;
    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        setContentView(binding.getRoot());

        ImageView logo = binding.splashScreenAppLogo;
        Glide.with(this).asBitmap().load(R.mipmap.ic_launcher_round).override(400).into(logo);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.splashScreenProgressBar.setVisibility(View.VISIBLE);
            checkAuthAndProceed();
        }, 500);
    }

    private void checkAuthAndProceed() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // Not signed in at all
            goToWelcome();
            return;
        }

        if (sessionManager.isLoggedIn()) {
            // Firebase session + backend session both valid → proceed
            goToHome();
            return;
        }

        // Firebase session exists but no local backend session → silently refresh
        Log.d(TAG, "Firebase user found but no local session. Refreshing backend session...");
        firebaseUser.getIdToken(true)
                .addOnSuccessListener(result -> {
                    String idToken = result.getToken();
                    if (idToken == null) {
                        goToWelcome();
                        return;
                    }
                    refreshBackendSession(idToken);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to refresh ID token", e);
                    goToWelcome();
                });
    }

    private void refreshBackendSession(String idToken) {
        ApiService api = RetrofitClient.getInstance().getApiService();
        api.customerFirebaseLogin(new CustomerAuthRequest(idToken))
                .enqueue(new Callback<CustomerAuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CustomerAuthResponse> call,
                            @NonNull Response<CustomerAuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            sessionManager.saveSession(response.body());
                            goToHome();
                        } else {
                            Log.e(TAG, "Backend session refresh failed: " + response.code());
                            goToWelcome();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CustomerAuthResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error during session refresh", t);
                        // Network is down — still go to Home since Firebase session is valid,
                        // individual screens can handle auth errors as they arise.
                        goToHome();
                    }
                });
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        String targetScreen = getIntent().getStringExtra(HomeActivity.EXTRA_TARGET_SCREEN);
        if (targetScreen != null && !targetScreen.isBlank()) {
            intent.putExtra(HomeActivity.EXTRA_TARGET_SCREEN, targetScreen);
        }
        startActivity(intent);
        finish();
    }

    private void goToWelcome() {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}
package com.akigen.ecotrekk.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivityHomeBinding;
import com.akigen.ecotrekk.fragment.CartFragment;
import com.akigen.ecotrekk.fragment.GearsFragment;
import com.akigen.ecotrekk.fragment.HomeFragment;
import com.akigen.ecotrekk.fragment.HubsFragment;
import com.akigen.ecotrekk.fragment.ProfileFragment;
import com.akigen.ecotrekk.manager.FcmManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    public static final String EXTRA_TARGET_SCREEN = "target_screen";
    public static final String TARGET_ORDER_HISTORY = "order_history";

    private ActivityHomeBinding activityHomeBinding;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, R.string.notifications_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());

        FcmManager.createNotificationChannel(this);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        bottomNavigationView = activityHomeBinding.homeActivityBottomNavigationView;
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }

        requestNotificationPermissionIfNeeded();
        handleNotificationNavigation(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationNavigation(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            bottomNavMenu.getItem(i).setChecked(false);
        }

        if (itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
            bottomNavMenu.findItem(R.id.bottom_nav_home).setChecked(true);
        } else if (itemId == R.id.bottom_nav_hubs) {
            loadFragment(new HubsFragment());
            bottomNavMenu.findItem(R.id.bottom_nav_hubs).setChecked(true);
        } else if (itemId == R.id.bottom_nav_gears) {
            loadFragment(new GearsFragment());
            bottomNavMenu.findItem(R.id.bottom_nav_gears).setChecked(true);
        } else if (itemId == R.id.bottom_nav_cart) {
            loadFragment(new CartFragment());
            bottomNavMenu.findItem(R.id.bottom_nav_cart).setChecked(true);
        } else if (itemId == R.id.bottom_nav_profile) {
            loadFragment(new ProfileFragment());
            bottomNavMenu.findItem(R.id.bottom_nav_profile).setChecked(true);
        }

        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.homeActivityFragmentContainerView, fragment)
                .commit();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void handleNotificationNavigation(Intent intent) {
        if (intent == null) {
            return;
        }

        String targetScreen = intent.getStringExtra(EXTRA_TARGET_SCREEN);
        if (TARGET_ORDER_HISTORY.equals(targetScreen)) {
            startActivity(new Intent(this, OrderHistoryActivity.class));
            intent.removeExtra(EXTRA_TARGET_SCREEN);
        }
    }
}
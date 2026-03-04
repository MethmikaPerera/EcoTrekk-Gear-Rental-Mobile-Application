package com.akigen.ecotrekk.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.databinding.ActivityHomeBinding;
import com.akigen.ecotrekk.fragment.CartFragment;
import com.akigen.ecotrekk.fragment.GearsFragment;
import com.akigen.ecotrekk.fragment.HomeFragment;
import com.akigen.ecotrekk.fragment.HubsFragment;
import com.akigen.ecotrekk.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityHomeBinding activityHomeBinding;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());

        bottomNavigationView = activityHomeBinding.homeActivityBottomNavigationView;
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }
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
}
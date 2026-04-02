package com.akigen.ecotrekk.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.AppSettingsActivity;
import com.akigen.ecotrekk.activity.LoginActivity;
import com.akigen.ecotrekk.activity.OrderHistoryActivity;
import com.akigen.ecotrekk.activity.PersonalInformationActivity;
import com.akigen.ecotrekk.api.ApiService;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.database.UserSessionManager;
import com.akigen.ecotrekk.databinding.FragmentProfileBinding;
import com.akigen.ecotrekk.manager.SessionManager;
import com.akigen.ecotrekk.model.User;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;
    private UserSessionManager userSessionManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(requireActivity());
        userSessionManager = UserSessionManager.getInstance(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            binding.profileUserName.setText(sessionManager.getFullName());
            String photoUrl = sessionManager.getPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .into(binding.profileAvatar);
                binding.profileAvatar.setVisibility(View.VISIBLE);
                binding.profileDefaultAvatar.setVisibility(View.INVISIBLE);
            } else {
                binding.profileAvatar.setVisibility(View.INVISIBLE);
                binding.profileDefaultAvatar.setVisibility(View.VISIBLE);
            }
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        setupBackPressNavigation();

        binding.profileLogoutBtn.setOnClickListener(v -> {
            deactivateDeviceTokensThenLogout();
        });

        binding.profilePersonalInfoItem.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
            startActivity(intent);
        });

        binding.profileOrderHistoryItem.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

        binding.profileAppSettingsItem.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AppSettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupBackPressNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.homeActivityBottomNavigationView);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
                    return;
                }

                setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void deactivateDeviceTokensThenLogout() {
        long customerId = sessionManager.getCustomerId();
        if (customerId <= 0) {
            proceedLogout();
            return;
        }

        ApiService api = RetrofitClient.getInstance().getApiService();
        api.deactivateAllDeviceTokens(customerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                proceedLogout();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                proceedLogout();
            }
        });
    }

    private void proceedLogout() {
        firebaseAuth.signOut();
        sessionManager.clearSession();
        userSessionManager.logoutAll();

        if (!isAdded() || getActivity() == null) {
            return;
        }

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        getActivity().finish();
    }
}
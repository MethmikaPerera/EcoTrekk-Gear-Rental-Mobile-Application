package com.akigen.ecotrekk.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.LoginActivity;
import com.akigen.ecotrekk.databinding.FragmentProfileBinding;
import com.akigen.ecotrekk.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
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
            String uid = currentUser.getUid();
            firebaseFirestore.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            binding.profileUserName.setText(user.getUname());
                            if (user.getProfilePicture() != null) {
                                Glide.with(this)
                                        .load(user.getProfilePicture())
                                        .into(binding.profileAvatar);
                                binding.profileAvatar.setVisibility(View.VISIBLE);
                                binding.profileDefaultAvatar.setVisibility(View.INVISIBLE);
                            } else {
                                binding.profileAvatar.setVisibility(View.INVISIBLE);
                                binding.profileDefaultAvatar.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        binding.profileLogoutBtn.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
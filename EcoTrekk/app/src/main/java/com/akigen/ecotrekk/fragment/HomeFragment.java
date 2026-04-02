package com.akigen.ecotrekk.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.LoginActivity;
import com.akigen.ecotrekk.activity.SingleGearActivity;
import com.akigen.ecotrekk.adapter.HomeCategoryAdapter;
import com.akigen.ecotrekk.adapter.HomeFeaturedGearAdapter;
import com.akigen.ecotrekk.adapter.HomeWishlistAdapter;
import com.akigen.ecotrekk.api.WishlistApiHelper;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.databinding.FragmentHomeBinding;
import com.akigen.ecotrekk.database.FavoritesHelper;
import com.akigen.ecotrekk.database.entity.FavoriteEntity;
import com.akigen.ecotrekk.dto.GearCategoryOptionResponse;
import com.akigen.ecotrekk.dto.PublicFeaturedGearResponse;
import com.akigen.ecotrekk.dto.WishlistItemResponse;
import com.akigen.ecotrekk.manager.SessionManager;
import com.akigen.ecotrekk.model.HomeCategory;
import com.akigen.ecotrekk.model.HomeFeaturedGear;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {

    private static final String RESULT_KEY_CATEGORY_FILTER = "result_key_category_filter";
    private static final String BUNDLE_KEY_CATEGORY_NAME = "bundle_key_category_name";

    private FragmentHomeBinding binding;
    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;
    private HomeCategoryAdapter homeCategoryAdapter;
    private HomeFeaturedGearAdapter homeFeaturedGearAdapter;
    private SideSheetBehavior<View> wishlistSideSheetBehavior;
    private FavoritesHelper favoritesHelper;
    private WishlistApiHelper wishlistApiHelper;
    private HomeWishlistAdapter wishlistAdapter;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(requireActivity());
        favoritesHelper = FavoritesHelper.getInstance(requireActivity());

        // Initialize for server sync
        Long customerId = sessionManager.getCustomerId();
        if (customerId > 0) {
            favoritesHelper.initializeWithCustomerId(customerId);
            wishlistApiHelper = new WishlistApiHelper(customerId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSideSheets();
        setupBackPressForSideSheets();
        setupRecyclerViews();
        setupSeeAllFeaturedGear();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            binding.homeUserNameTxt.setText(sessionManager.getFullName());
            fetchFeaturedGearFromBackend();
            fetchExploreCategoriesFromBackend();
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wishlistSideSheetBehavior != null
                && wishlistSideSheetBehavior.getState() == SideSheetBehavior.STATE_EXPANDED) {
            loadWishlistItems();
        }
    }

    private void setupSideSheets() {
        wishlistSideSheetBehavior = SideSheetBehavior.from(binding.wishlistSideSheet);

        wishlistSideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN);

        // Setup wishlist RecyclerView
        wishlistAdapter = new HomeWishlistAdapter(this::openSingleGearDetails);
        binding.rvWishlistItems.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        binding.rvWishlistItems.setHasFixedSize(false);
        binding.rvWishlistItems.setNestedScrollingEnabled(true);
        binding.rvWishlistItems.setAdapter(wishlistAdapter);

        binding.btnOpenWishlistSheet.setOnClickListener(v -> {
            wishlistSideSheetBehavior.setState(SideSheetBehavior.STATE_EXPANDED);
            // Load wishlist when opening
            loadWishlistItems();
        });

        binding.btnCloseWishlistSheet.setOnClickListener(v ->
                wishlistSideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN)
        );
    }

    private void setupBackPressForSideSheets() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (wishlistSideSheetBehavior != null
                        && wishlistSideSheetBehavior.getState() != SideSheetBehavior.STATE_HIDDEN) {
                    wishlistSideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN);
                    return;
                }

                setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setupRecyclerViews() {
        homeCategoryAdapter = new HomeCategoryAdapter(this::openGearsWithCategoryFilter);
        homeFeaturedGearAdapter = new HomeFeaturedGearAdapter(this::openSingleGearDetails);

        binding.recyclerHomeCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerHomeCategories.setHasFixedSize(true);
        binding.recyclerHomeCategories.setNestedScrollingEnabled(false);
        binding.recyclerHomeCategories.setAdapter(homeCategoryAdapter);

        binding.recyclerFeaturedGear.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerFeaturedGear.setHasFixedSize(false);
        binding.recyclerFeaturedGear.setNestedScrollingEnabled(true);
        binding.recyclerFeaturedGear.setAdapter(homeFeaturedGearAdapter);
    }

    private void openSingleGearDetails(HomeFeaturedGear gear) {
        Intent intent = new Intent(requireContext(), SingleGearActivity.class);
        intent.putExtra("gear_id", gear.getId() != null ? gear.getId() : -1L);
        intent.putExtra("gear_name", gear.getName());
        intent.putExtra("gear_category", gear.getCategory());
        intent.putExtra("gear_description", gear.getDescription() != null ? gear.getDescription() : "");
        intent.putExtra("gear_image_url", gear.getImageUrl());
        intent.putExtra("gear_price", gear.getPerDayPrice());
        intent.putExtra("gear_deposit", gear.getDepositAmount());
        intent.putExtra("gear_image_res_id", gear.getImageResId());
        intent.putExtra("gear_available_qty", gear.getAvailableQty());
        startActivity(intent);
    }

    private void openGearsWithCategoryFilter(HomeCategory category) {
        Bundle result = new Bundle();
        result.putString(BUNDLE_KEY_CATEGORY_NAME, category.getName());
        getParentFragmentManager().setFragmentResult(RESULT_KEY_CATEGORY_FILTER, result);

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.homeActivityBottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_gears);
        }
    }

    private void setupSeeAllFeaturedGear() {
        binding.featuredGearSeeAllTxtBtn.setOnClickListener(v -> {
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.homeActivityBottomNavigationView);
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.bottom_nav_gears);
            }
        });
    }

    private void fetchExploreCategoriesFromBackend() {
        RetrofitClient.getInstance()
                .getApiService()
                .getPublicGearCategories()
                .enqueue(new Callback<List<GearCategoryOptionResponse>>() {
                    @Override
                    public void onResponse(Call<List<GearCategoryOptionResponse>> call, Response<List<GearCategoryOptionResponse>> response) {
                        if (!isAdded() || binding == null) {
                            return;
                        }

                        List<GearCategoryOptionResponse> apiCategories = response.body();
                        if (!response.isSuccessful() || apiCategories == null || apiCategories.isEmpty()) {
                            clearExploreCategories();
                            return;
                        }

                        List<HomeCategory> mapped = apiCategories.stream()
                                .filter(category -> category.getName() != null && !category.getName().trim().isEmpty())
                                .map(category -> new HomeCategory(
                                        category.getName().trim(),
                                        "Available",
                                        category.getImageUrl(),
                                        resolveCategoryImageRes(category.getName())
                                ))
                                .toList();

                        if (mapped.isEmpty()) {
                            clearExploreCategories();
                            return;
                        }

                        homeCategoryAdapter.submitList(mapped);
                    }

                    @Override
                    public void onFailure(Call<List<GearCategoryOptionResponse>> call, Throwable throwable) {
                        if (!isAdded() || binding == null) {
                            return;
                        }
                        clearExploreCategories();
                    }
                });
    }

    private void fetchFeaturedGearFromBackend() {
        RetrofitClient.getInstance()
                .getApiService()
                .getPublicFeaturedGears(6)
                .enqueue(new Callback<List<PublicFeaturedGearResponse>>() {
                    @Override
                    public void onResponse(Call<List<PublicFeaturedGearResponse>> call, Response<List<PublicFeaturedGearResponse>> response) {
                        if (!isAdded() || binding == null) {
                            return;
                        }

                        List<PublicFeaturedGearResponse> apiFeatured = response.body();
                        if (!response.isSuccessful() || apiFeatured == null || apiFeatured.isEmpty()) {
                            clearFeaturedGear();
                            return;
                        }

                        List<HomeFeaturedGear> mapped = apiFeatured.stream()
                                .filter(gear -> gear.getName() != null && !gear.getName().trim().isEmpty())
                                .map(gear -> new HomeFeaturedGear(
                                gear.getId(),
                                        gear.getName().trim(),
                                        gear.getCategory() != null ? gear.getCategory().trim() : "Other",
                                gear.getDescription() != null ? gear.getDescription() : "",
                                        gear.getPricePerDay(),
                                gear.getDepositAmount(),
                                        gear.getImageUrl(),
                                        resolveCategoryImageRes(gear.getCategory()),
                                        gear.getAvailableQty() != null ? gear.getAvailableQty() : Integer.MAX_VALUE
                                ))
                                .toList();

                        if (mapped.isEmpty()) {
                            clearFeaturedGear();
                            return;
                        }

                        homeFeaturedGearAdapter.submitList(mapped);
                    }

                    @Override
                    public void onFailure(Call<List<PublicFeaturedGearResponse>> call, Throwable throwable) {
                        if (!isAdded() || binding == null) {
                            return;
                        }
                        clearFeaturedGear();
                    }
                });
    }

    private int resolveCategoryImageRes(String categoryName) {
        String normalized = categoryName == null ? "" : categoryName.trim().toLowerCase(Locale.US);
        if (normalized.contains("backpack") || normalized.contains("pack")) {
            return R.drawable.ecotrekk_notfound;
        }
        if (normalized.contains("tent") || normalized.contains("camp")) {
            return R.drawable.ecotrekk_notfound;
        }
        if (normalized.contains("cook")) {
            return R.drawable.ecotrekk_notfound;
        }
        if (normalized.contains("sleep")) {
            return R.drawable.ecotrekk_notfound;
        }
        return R.drawable.ecotrekk_notfound;
    }

    private void clearExploreCategories() {
        homeCategoryAdapter.submitList(List.of());
    }

    private void clearFeaturedGear() {
        homeFeaturedGearAdapter.submitList(List.of());
    }

    private void loadWishlistItems() {
        if (wishlistApiHelper != null) {
            loadWishlistFromServer();
            return;
        }
        loadWishlistFromLocal();
    }

    private void loadWishlistFromServer() {
        wishlistApiHelper.fetchWishlist(new WishlistApiHelper.WishlistCallback<List<WishlistItemResponse>>() {
            @Override
            public void onSuccess(List<WishlistItemResponse> wishlistItems) {
                if (!isAdded() || binding == null) {
                    return;
                }

                List<HomeFeaturedGear> gearList = new ArrayList<>();
                if (wishlistItems != null) {
                    for (WishlistItemResponse item : wishlistItems) {
                        gearList.add(new HomeFeaturedGear(
                                item.getGearId(),
                                item.getGearName(),
                                item.getCategory() != null ? item.getCategory() : "Other",
                                item.getDescription() != null ? item.getDescription() : "",
                                item.getPricePerDay() != null ? item.getPricePerDay() : 0.0,
                                item.getDepositAmount() != null ? item.getDepositAmount() : 0.0,
                                item.getImageUrl(),
                                R.drawable.ecotrekk_notfound
                        ));
                    }
                }
                renderWishlist(gearList);
            }

            @Override
            public void onError(String message) {
                loadWishlistFromLocal();
            }
        });
    }

    private void loadWishlistFromLocal() {
        ioExecutor.execute(() -> {
            List<FavoriteEntity> favorites = favoritesHelper.getAllFavorites();
            List<HomeFeaturedGear> mapped = new ArrayList<>();
            if (favorites != null) {
                for (FavoriteEntity favorite : favorites) {
                    mapped.add(new HomeFeaturedGear(
                            favorite.getGearId(),
                            favorite.getGearName(),
                            favorite.getCategory() != null ? favorite.getCategory() : "Other",
                            favorite.getDescription() != null ? favorite.getDescription() : "",
                            favorite.getPricePerDay(),
                            favorite.getDepositAmount(),
                            favorite.getImageUrl(),
                            R.drawable.ecotrekk_notfound
                    ));
                }
            }

            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> renderWishlist(mapped));
        });
    }

    private void renderWishlist(List<HomeFeaturedGear> items) {
        if (!isAdded() || binding == null) {
            return;
        }

        if (items == null || items.isEmpty()) {
            binding.txtWishlistEmpty.setVisibility(View.VISIBLE);
            binding.rvWishlistItems.setVisibility(View.GONE);
            wishlistAdapter.submitList(List.of());
            return;
        }

        binding.txtWishlistEmpty.setVisibility(View.GONE);
        binding.rvWishlistItems.setVisibility(View.VISIBLE);
        wishlistAdapter.submitList(items);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }
}
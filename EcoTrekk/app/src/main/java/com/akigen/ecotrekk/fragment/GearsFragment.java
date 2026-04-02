package com.akigen.ecotrekk.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.SingleGearActivity;
import com.akigen.ecotrekk.adapter.GearProductAdapter;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.GearCategoryOptionResponse;
import com.akigen.ecotrekk.databinding.FragmentGearsBinding;
import com.akigen.ecotrekk.dto.PublicFeaturedGearResponse;
import com.akigen.ecotrekk.model.GearProduct;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GearsFragment extends Fragment {

    private static final int DEFAULT_SORT_OPTION_INDEX = 0;
    private static final float FALLBACK_MIN_PRICE = 0f;
    private static final float FALLBACK_MAX_PRICE = 1000f;
    private static final String RESULT_KEY_CATEGORY_FILTER = "result_key_category_filter";
    private static final String BUNDLE_KEY_CATEGORY_NAME = "bundle_key_category_name";

    private FragmentGearsBinding binding;
    private GearProductAdapter gearProductAdapter;
    private List<GearProduct> allGearProducts;
    private SideSheetBehavior<View> sideSheetBehavior;
    private float minProductPrice = FALLBACK_MIN_PRICE;
    private float maxProductPrice = FALLBACK_MAX_PRICE;
    private String pendingCategoryFilter;
    private List<String> categoryOptions = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float accel = 0.00f;
    private float accelCurrent = SensorManager.GRAVITY_EARTH;
    private float accelLast = SensorManager.GRAVITY_EARTH;
    private long lastShakeTime = 0;

    public GearsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGearsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
                RESULT_KEY_CATEGORY_FILTER,
                this,
                (requestKey, result) -> {
                    pendingCategoryFilter = result.getString(BUNDLE_KEY_CATEGORY_NAME);
                    applyCategoryFilterIfReady();
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSideSheet();
        setupBackPressNavigation();
        setupRecyclerView();
        allGearProducts = new ArrayList<>();
        refreshPriceBoundsFromCurrentData();
        setupFiltersAndSearch();
        applyCategoryFilterIfReady();
        setupAppliedChipActions();
        applyFilters();
        setupShakeToRefresh();
        fetchGearItemsFromBackend();
    }

    private void applyCategoryFilterIfReady() {
        if (binding == null || pendingCategoryFilter == null || pendingCategoryFilter.trim().isEmpty()) {
            return;
        }

        String targetCategory = resolveCategoryNameForSelection(pendingCategoryFilter);
        pendingCategoryFilter = null;
        if (targetCategory == null) {
            return;
        }

        binding.autoCompleteCategory.setText(targetCategory, false);
        applyFilters();
    }

    private String resolveCategoryNameForSelection(String categoryName) {
        String normalized = categoryName.trim().toLowerCase(Locale.US);

        for (String option : categoryOptions) {
            if (option != null && option.trim().equalsIgnoreCase(normalized)) {
                return option;
            }
        }

        if (normalized.equals("all")) {
            return getString(R.string.filter_category_all);
        }

        if (normalized.equals("backpack") || normalized.equals("backpacking")) {
            return findMatchingCategoryOption("backpack");
        }
        if (normalized.equals("tent") || normalized.contains("camp")) {
            return findMatchingCategoryOption("tent");
        }
        if (normalized.equals("cooking") || normalized.contains("cook")) {
            return findMatchingCategoryOption("cook");
        }
        if (normalized.equals("sleeping") || normalized.contains("sleep")) {
            return findMatchingCategoryOption("sleep");
        }

        return getString(R.string.filter_category_all);
    }

    private String findMatchingCategoryOption(String token) {
        for (String option : categoryOptions) {
            if (option != null && option.toLowerCase(Locale.US).contains(token)) {
                return option;
            }
        }
        return getString(R.string.filter_category_all);
    }

    private void setupBackPressNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (sideSheetBehavior != null && sideSheetBehavior.getState() == SideSheetBehavior.STATE_EXPANDED) {
                    sideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN);
                    return;
                }

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

    private void setupAppliedChipActions() {
        binding.chipAppliedSearch.setOnCloseIconClickListener(v -> binding.editGearSearch.setText(""));
        binding.chipAppliedCategory.setOnCloseIconClickListener(v -> {
            binding.autoCompleteCategory.setText(getString(R.string.filter_category_all), false);
            applyFilters();
        });
        binding.chipAppliedPrice.setOnCloseIconClickListener(v -> binding.rangePriceFilter.setValues(minProductPrice, maxProductPrice));
    }

    private void setupSideSheet() {
        sideSheetBehavior = SideSheetBehavior.from(binding.filterSideSheet);
        sideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN);

        binding.btnOpenFilterSheet.setOnClickListener(v -> sideSheetBehavior.setState(SideSheetBehavior.STATE_EXPANDED));
        binding.btnCloseFilterSheet.setOnClickListener(v -> sideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN));
    }

    private void setupRecyclerView() {
        gearProductAdapter = new GearProductAdapter(this::openSingleGearDetails);
        binding.recyclerGears.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerGears.setAdapter(gearProductAdapter);
    }

    private void openSingleGearDetails(GearProduct product) {
        Intent intent = new Intent(requireContext(), SingleGearActivity.class);
        intent.putExtra("gear_id", product.getId());
        intent.putExtra("gear_name", product.getName());
        intent.putExtra("gear_category", product.getCategory());
        intent.putExtra("gear_description", product.getDescription());
        intent.putExtra("gear_image_url", product.getImageUrl());
        intent.putExtra("gear_price", product.getPerDayPrice());
        intent.putExtra("gear_deposit", product.getDepositAmount());
        intent.putExtra("gear_image_res_id", product.getImageResId());
        intent.putExtra("gear_available_qty", product.getAvailableQty());
        startActivity(intent);
    }

    private void setupFiltersAndSearch() {
        setupPriceSlider();
        setupCategoryDropdown();

        binding.editGearSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });

        String[] sortOptions = new String[]{
            getString(R.string.sort_name_asc),
            getString(R.string.sort_price_low_high),
            getString(R.string.sort_price_high_low)
        };

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            sortOptions
        );
        binding.autoCompleteSort.setAdapter(sortAdapter);
        binding.autoCompleteSort.setText(sortOptions[DEFAULT_SORT_OPTION_INDEX], false);
        binding.autoCompleteSort.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        fetchCategoriesFromBackend();
    }

    private void setupCategoryDropdown() {
        updateCategoryDropdownOptions(List.of());
        binding.autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> applyFilters());
    }

    private void fetchCategoriesFromBackend() {
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
                            return;
                        }

                        List<String> mapped = apiCategories.stream()
                                .map(GearCategoryOptionResponse::getName)
                                .filter(name -> name != null && !name.trim().isEmpty())
                                .map(String::trim)
                                .distinct()
                                .toList();

                        if (mapped.isEmpty()) {
                            return;
                        }

                        updateCategoryDropdownOptions(mapped);
                        applyCategoryFilterIfReady();
                        applyFilters();
                    }

                    @Override
                    public void onFailure(Call<List<GearCategoryOptionResponse>> call, Throwable throwable) {
                        // Keep default category options when backend categories are unavailable.
                    }
                });
    }

    private void updateCategoryDropdownOptions(List<String> categories) {
        String allCategoryLabel = getString(R.string.filter_category_all);

        List<String> options = new ArrayList<>();
        options.add(allCategoryLabel);

        for (String category : categories) {
            if (category == null || category.trim().isEmpty()) {
                continue;
            }
            String normalized = category.trim();
            if (normalized.equalsIgnoreCase(allCategoryLabel)) {
                continue;
            }
            boolean exists = options.stream().anyMatch(item -> item.equalsIgnoreCase(normalized));
            if (!exists) {
                options.add(normalized);
            }
        }

        categoryOptions = options;
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryOptions
        );
        binding.autoCompleteCategory.setAdapter(categoryAdapter);

        String currentSelection = binding.autoCompleteCategory.getText() == null
                ? ""
                : binding.autoCompleteCategory.getText().toString().trim();
        boolean currentIsValid = categoryOptions.stream().anyMatch(option -> option.equalsIgnoreCase(currentSelection));

        if (!currentIsValid) {
            binding.autoCompleteCategory.setText(allCategoryLabel, false);
        }
    }

    private void setupPriceSlider() {
        binding.rangePriceFilter.clearOnChangeListeners();
        binding.rangePriceFilter.setValueFrom(minProductPrice);
        binding.rangePriceFilter.setValueTo(maxProductPrice);
        binding.rangePriceFilter.setStepSize(0f);
        binding.rangePriceFilter.setValues(minProductPrice, maxProductPrice);
        updateSelectedPriceText(minProductPrice, maxProductPrice);

        binding.rangePriceFilter.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
                float minSelected = values.get(0);
                float maxSelected = values.get(1);
                updateSelectedPriceText(minSelected, maxSelected);
            }
            applyFilters();
        });
    }

    private void updateSelectedPriceText(float minPrice, float maxPrice) {
        binding.txtSelectedPriceRange.setText(getString(
                R.string.filter_price_selected_range,
                Math.round(minPrice),
                Math.round(maxPrice)
        ));
    }

    private void refreshPriceBoundsFromCurrentData() {
        if (allGearProducts == null || allGearProducts.isEmpty()) {
            minProductPrice = FALLBACK_MIN_PRICE;
            maxProductPrice = FALLBACK_MAX_PRICE;
            return;
        }

        double lowestPrice = allGearProducts.stream()
                .mapToDouble(GearProduct::getPerDayPrice)
                .min()
                .orElse(FALLBACK_MIN_PRICE);
        double highestPrice = allGearProducts.stream()
                .mapToDouble(GearProduct::getPerDayPrice)
                .max()
                .orElse(FALLBACK_MAX_PRICE);

        minProductPrice = (float) Math.floor(lowestPrice);
        maxProductPrice = (float) Math.ceil(highestPrice);

        if (maxProductPrice <= minProductPrice) {
            maxProductPrice = minProductPrice + 1f;
        }
    }

    private void fetchGearItemsFromBackend() {
        RetrofitClient.getInstance()
                .getApiService()
                .getPublicGears()
                .enqueue(new Callback<List<PublicFeaturedGearResponse>>() {
                    @Override
                    public void onResponse(Call<List<PublicFeaturedGearResponse>> call, Response<List<PublicFeaturedGearResponse>> response) {
                        if (!isAdded() || binding == null) {
                            return;
                        }

                        List<PublicFeaturedGearResponse> apiGears = response.body();
                        if (!response.isSuccessful() || apiGears == null || apiGears.isEmpty()) {
                            return;
                        }

                        List<GearProduct> mapped = apiGears.stream()
                                .filter(gear -> gear.getName() != null && !gear.getName().trim().isEmpty())
                                .map(gear -> new GearProduct(
                                        gear.getId() != null ? gear.getId() : -1L,
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
                            return;
                        }

                        allGearProducts = new ArrayList<>(mapped);
                        refreshPriceBoundsFromCurrentData();
                        setupPriceSlider();
                        applyFilters();
                    }

                    @Override
                    public void onFailure(Call<List<PublicFeaturedGearResponse>> call, Throwable throwable) {
                        // Keep local fallback data when backend is unavailable.
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
            return R.drawable.welcome_hiker;
        }
        return R.drawable.ecotrekk_notfound;
    }

    private void applyFilters() {
        String query = "";
        if (binding.editGearSearch.getText() != null) {
            query = binding.editGearSearch.getText().toString().trim();
        }
        String normalizedQuery = query.toLowerCase(Locale.US);

        String selectedCategory = getSelectedCategory();
        List<Float> selectedPrices = binding.rangePriceFilter.getValues();
        float minSelectedPrice = selectedPrices.get(0);
        float maxSelectedPrice = selectedPrices.get(1);

        List<GearProduct> filtered = new ArrayList<>();
        for (GearProduct product : allGearProducts) {
            boolean matchesSearch = normalizedQuery.isEmpty() || product.getName().toLowerCase(Locale.US).contains(normalizedQuery);
            boolean matchesCategory = "All".equals(selectedCategory) || selectedCategory.equals(product.getCategory());
            boolean matchesPrice = product.getPerDayPrice() >= minSelectedPrice
                    && product.getPerDayPrice() <= maxSelectedPrice;

            if (matchesSearch && matchesCategory && matchesPrice) {
                filtered.add(product);
            }
        }

        sortProducts(filtered);
        updateAppliedFilterChips(query, selectedCategory, minSelectedPrice, maxSelectedPrice);

        gearProductAdapter.submitList(filtered);
        toggleEmptyState(filtered.isEmpty());
    }

    private void updateAppliedFilterChips(String query, String selectedCategory, float minSelectedPrice, float maxSelectedPrice) {
        boolean isSearchApplied = !query.isEmpty();
        boolean isCategoryApplied = !"All".equals(selectedCategory);
        boolean isPriceApplied = minSelectedPrice > minProductPrice || maxSelectedPrice < maxProductPrice;
        boolean hasAnyAppliedFilter = isSearchApplied || isCategoryApplied || isPriceApplied;

        binding.cardAppliedFilters.setVisibility(hasAnyAppliedFilter ? View.VISIBLE : View.GONE);

        if (isSearchApplied) {
            binding.chipAppliedSearch.setText(getString(R.string.applied_search_format, query));
            binding.chipAppliedSearch.setVisibility(View.VISIBLE);
        } else {
            binding.chipAppliedSearch.setVisibility(View.GONE);
        }

        if (isCategoryApplied) {
            binding.chipAppliedCategory.setText(getString(R.string.applied_category_format, selectedCategory));
            binding.chipAppliedCategory.setVisibility(View.VISIBLE);
        } else {
            binding.chipAppliedCategory.setVisibility(View.GONE);
        }

        if (isPriceApplied) {
            binding.chipAppliedPrice.setText(getString(
                    R.string.applied_price_format,
                    Math.round(minSelectedPrice),
                    Math.round(maxSelectedPrice)
            ));
            binding.chipAppliedPrice.setVisibility(View.VISIBLE);
        } else {
            binding.chipAppliedPrice.setVisibility(View.GONE);
        }
    }

    private void sortProducts(List<GearProduct> products) {
        String sortSelection = binding.autoCompleteSort.getText() == null
                ? ""
                : binding.autoCompleteSort.getText().toString();

        if (getString(R.string.sort_price_low_high).equals(sortSelection)) {
            products.sort(Comparator.comparingDouble(GearProduct::getPerDayPrice));
            return;
        }

        if (getString(R.string.sort_price_high_low).equals(sortSelection)) {
            products.sort((first, second) -> Double.compare(second.getPerDayPrice(), first.getPerDayPrice()));
            return;
        }

        products.sort((first, second) -> first.getName().compareToIgnoreCase(second.getName()));
    }

    private String getSelectedCategory() {
        String selected = binding.autoCompleteCategory.getText() == null
                ? ""
                : binding.autoCompleteCategory.getText().toString().trim();

        if (selected.isEmpty() || selected.equalsIgnoreCase(getString(R.string.filter_category_all))) {
            return "All";
        }

        return selected;
    }

    private void toggleEmptyState(boolean isEmpty) {
        binding.txtEmptyGears.setVisibility(isEmpty ? TextView.VISIBLE : TextView.GONE);
        binding.recyclerGears.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupShakeToRefresh() {
        if (getContext() != null) {
            sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }
    }

    private final SensorEventListener shakeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = accelCurrent - accelLast;
            accel = accel * 0.9f + delta; // perform low-cut filter
            
            if (accel > 12) { // Shake threshold gravity
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 2000) {
                    lastShakeTime = currentTime;
                    triggerShakeRefresh();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No-op
        }
    };

    private void triggerShakeRefresh() {
        if (!isAdded() || binding == null) {
            return;
        }
        Snackbar.make(binding.getRoot(), "Refreshing catalog...", Snackbar.LENGTH_SHORT).show();
        fetchGearItemsFromBackend();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

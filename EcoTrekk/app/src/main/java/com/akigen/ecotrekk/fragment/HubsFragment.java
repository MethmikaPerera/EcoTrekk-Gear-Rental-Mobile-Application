package com.akigen.ecotrekk.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.activity.SingleHubActivity;
import com.akigen.ecotrekk.adapter.NearbyHubAdapter;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.databinding.FragmentHubsBinding;
import com.akigen.ecotrekk.dto.PublicHubResponse;
import com.akigen.ecotrekk.manager.ThemePreferenceManager;
import com.akigen.ecotrekk.model.HubLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HubsFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng SRI_LANKA_CENTER = new LatLng(7.8731, 80.7718);
    private static final float COUNTRY_ZOOM = 7f;
    private static final float HUB_FOCUS_ZOOM = 11.8f;
    private static final int SHEET_PEEK_HEIGHT_DP = 220;
    private static final String EXTRA_HUB_NAME = "hub_name";
    private static final String EXTRA_HUB_CITY = "hub_city";
    private static final String EXTRA_HUB_DISTANCE = "hub_distance";
    private static final String EXTRA_HUB_HOURS = "hub_hours";
    private static final String EXTRA_HUB_LATITUDE = "hub_latitude";
    private static final String EXTRA_HUB_LONGITUDE = "hub_longitude";
    private static final String EXTRA_HUB_PHONE = "hub_phone";
    private static final String EXTRA_HUB_IMAGE_URL = "hub_image_url";

    private FragmentHubsBinding binding;
    private NearbyHubAdapter nearbyHubAdapter;
    private GoogleMap googleMap;
    private BottomSheetBehavior<MaterialCardView> nearbyHubsSheetBehavior;
    private final List<HubLocation> allHubs = new ArrayList<>();
    private final Map<String, Marker> markersByName = new LinkedHashMap<>();
    @Nullable
    private LatLng currentUserLatLng;
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (fineGranted || coarseGranted) {
                    resolveCurrentLocationAndRefreshDistances();
                }
                updateMyLocationLayer();
            });

    public HubsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHubsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackPressNavigation();
        setupRecyclerView();
        setupNearbyHubsBottomSheet();
        setupMap();
        fetchHubsFromBackend();
        requestLocationAndRefreshDistances();
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

    private void setupNearbyHubsBottomSheet() {
        nearbyHubsSheetBehavior = BottomSheetBehavior.from(binding.cardNearbyHubsSheet);
        nearbyHubsSheetBehavior.setHideable(false);
        nearbyHubsSheetBehavior.setSkipCollapsed(false);
        nearbyHubsSheetBehavior.setPeekHeight(dpToPx(SHEET_PEEK_HEIGHT_DP), true);
        nearbyHubsSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        nearbyHubsSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                updateMapPaddingFromSheet(bottomSheet);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                updateMapPaddingFromSheet(bottomSheet);
            }
        });

        binding.cardNearbyHubsSheet.post(() -> updateMapPaddingFromSheet(binding.cardNearbyHubsSheet));
    }

    private void setupRecyclerView() {
        nearbyHubAdapter = new NearbyHubAdapter(new NearbyHubAdapter.OnHubActionListener() {
            @Override
            public void onHubSelected(HubLocation hubLocation) {
                focusHubOnMap(hubLocation, true);
            }

            @Override
            public void onViewDetailsClicked(HubLocation hubLocation) {
                openHubDetails(hubLocation);
            }
        });

        binding.recyclerNearbyHubs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerNearbyHubs.setAdapter(nearbyHubAdapter);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.hubsMapFragmentContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void displayAllHubs() {
        nearbyHubAdapter.submitList(allHubs);
        renderMarkers(allHubs);
    }

    private void fetchHubsFromBackend() {
        RetrofitClient.getInstance()
                .getApiService()
                .getPublicHubs()
                .enqueue(new Callback<List<PublicHubResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PublicHubResponse>> call,
                                           @NonNull Response<List<PublicHubResponse>> response) {
                        if (!isAdded() || binding == null) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Snackbar.make(binding.getRoot(), R.string.hubs_load_failed, Snackbar.LENGTH_SHORT).show();
                            allHubs.clear();
                            displayAllHubs();
                            return;
                        }

                        allHubs.clear();
                        for (PublicHubResponse publicHub : response.body()) {
                            allHubs.add(mapToHubLocation(publicHub));
                        }

                        displayAllHubs();
                        if (allHubs.isEmpty()) {
                            Snackbar.make(binding.getRoot(), R.string.hubs_empty_state, Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PublicHubResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) {
                            return;
                        }
                        Snackbar.make(binding.getRoot(), R.string.hubs_load_failed, Snackbar.LENGTH_SHORT).show();
                        allHubs.clear();
                        displayAllHubs();
                    }
                });
    }

    private HubLocation mapToHubLocation(@NonNull PublicHubResponse publicHub) {
        String name = nonEmpty(publicHub.getName(), getString(R.string.hubs_default_name));
        String address = nonEmpty(publicHub.getAddress(), getString(R.string.hubs_default_address));

        return new HubLocation(
                name,
                address,
            nonEmpty(publicHub.getPhone(), ""),
            nonEmpty(publicHub.getImageUrl(), ""),
                buildDistanceText(publicHub.getLatitude(), publicHub.getLongitude()),
                getString(R.string.hubs_open_now),
                publicHub.getLatitude(),
                publicHub.getLongitude()
        );
    }

    private void requestLocationAndRefreshDistances() {
        if (hasLocationPermission()) {
            updateMyLocationLayer();
            resolveCurrentLocationAndRefreshDistances();
            return;
        }

        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private boolean hasLocationPermission() {
        Context context = getContext();
        if (context == null) {
            return false;
        }

        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void resolveCurrentLocationAndRefreshDistances() {
        Context context = getContext();
        if (context == null || !hasLocationPermission()) {
            return;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return;
        }

        Location bestKnownLocation = null;
        String[] providers = new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };

        for (String provider : providers) {
            Location candidate = locationManager.getLastKnownLocation(provider);
            if (candidate == null) {
                continue;
            }
            if (bestKnownLocation == null || candidate.getTime() > bestKnownLocation.getTime()) {
                bestKnownLocation = candidate;
            }
        }

        if (bestKnownLocation == null) {
            return;
        }

        currentUserLatLng = new LatLng(bestKnownLocation.getLatitude(), bestKnownLocation.getLongitude());
        refreshDistancesForVisibleHubs();
    }

    private void refreshDistancesForVisibleHubs() {
        if (allHubs.isEmpty()) {
            return;
        }

        List<HubLocation> refreshedHubs = new ArrayList<>(allHubs.size());
        for (HubLocation hub : allHubs) {
            refreshedHubs.add(new HubLocation(
                    hub.getName(),
                    hub.getCity(),
                    hub.getPhone(),
                    hub.getImageUrl(),
                    buildDistanceText(hub.getLatitude(), hub.getLongitude()),
                    hub.getOpenHoursText(),
                    hub.getLatitude(),
                    hub.getLongitude()
            ));
        }

        allHubs.clear();
        allHubs.addAll(refreshedHubs);
        displayAllHubs();
    }

    private String buildDistanceText(double hubLatitude, double hubLongitude) {
        if (currentUserLatLng == null) {
            return getString(R.string.hubs_distance_unavailable);
        }

        float[] results = new float[1];
        Location.distanceBetween(
                currentUserLatLng.latitude,
                currentUserLatLng.longitude,
                hubLatitude,
                hubLongitude,
                results
        );

        if (results[0] <= 0f || Float.isNaN(results[0])) {
            return getString(R.string.hubs_distance_unavailable);
        }

        return getString(R.string.hubs_distance_km, results[0] / 1000f);
    }

    @SuppressLint("MissingPermission")
    private void updateMyLocationLayer() {
        if (googleMap == null) {
            return;
        }

        boolean hasPermission = hasLocationPermission();
        googleMap.getUiSettings().setMyLocationButtonEnabled(hasPermission);
        googleMap.setMyLocationEnabled(hasPermission);
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private void renderMarkers(List<HubLocation> hubs) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        markersByName.clear();

        for (HubLocation hub : hubs) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(hub.getLatitude(), hub.getLongitude()))
                    .title(hub.getName())
                    .snippet(hub.getCity()));

            if (marker != null) {
                markersByName.put(hub.getName(), marker);
            }
        }
    }

    private void focusHubOnMap(HubLocation hubLocation, boolean showInfoWindow) {
        if (googleMap == null) {
            return;
        }

        LatLng point = new LatLng(hubLocation.getLatitude(), hubLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, HUB_FOCUS_ZOOM));

        if (showInfoWindow) {
            Marker marker = markersByName.get(hubLocation.getName());
            if (marker != null) {
                marker.showInfoWindow();
            }
        }
    }

    private void openHubDetails(HubLocation hubLocation) {
        Intent detailsIntent = new Intent(requireContext(), SingleHubActivity.class);
        detailsIntent.putExtra(EXTRA_HUB_NAME, hubLocation.getName());
        detailsIntent.putExtra(EXTRA_HUB_CITY, hubLocation.getCity());
        detailsIntent.putExtra(EXTRA_HUB_DISTANCE, hubLocation.getDistanceText());
        detailsIntent.putExtra(EXTRA_HUB_HOURS, hubLocation.getOpenHoursText());
        detailsIntent.putExtra(EXTRA_HUB_LATITUDE, hubLocation.getLatitude());
        detailsIntent.putExtra(EXTRA_HUB_LONGITUDE, hubLocation.getLongitude());
        detailsIntent.putExtra(EXTRA_HUB_PHONE, hubLocation.getPhone());
        detailsIntent.putExtra(EXTRA_HUB_IMAGE_URL, hubLocation.getImageUrl());
        startActivity(detailsIntent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        updateMyLocationLayer();

        int topPadding = dpToPx(86);
        int bottomPadding = dpToPx(SHEET_PEEK_HEIGHT_DP + 20);
        googleMap.setPadding(0, topPadding, 0, bottomPadding);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SRI_LANKA_CENTER, COUNTRY_ZOOM));
        displayAllHubs();
    }

    private void updateMapPaddingFromSheet(@NonNull View bottomSheet) {
        if (googleMap == null || binding == null) {
            return;
        }

        int topPadding = dpToPx(86);
        int bottomPadding = Math.max(dpToPx(150), binding.getRoot().getHeight() - bottomSheet.getTop() + dpToPx(12));
        googleMap.setPadding(0, topPadding, 0, bottomPadding);
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        googleMap = null;
        nearbyHubsSheetBehavior = null;
        currentUserLatLng = null;
        markersByName.clear();
    }
}
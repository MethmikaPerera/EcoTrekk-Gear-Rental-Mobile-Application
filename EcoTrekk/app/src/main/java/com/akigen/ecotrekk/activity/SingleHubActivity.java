package com.akigen.ecotrekk.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.akigen.ecotrekk.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class SingleHubActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_HUB_NAME = "hub_name";
    private static final String EXTRA_HUB_CITY = "hub_city";
    private static final String EXTRA_HUB_DISTANCE = "hub_distance";
    private static final String EXTRA_HUB_HOURS = "hub_hours";
    private static final String EXTRA_HUB_LATITUDE = "hub_latitude";
    private static final String EXTRA_HUB_LONGITUDE = "hub_longitude";
    private static final String EXTRA_HUB_PHONE = "hub_phone";
    private static final String EXTRA_HUB_IMAGE_URL = "hub_image_url";
    private static final String MAP_VIEW_BUNDLE_KEY = "single_hub_map_preview_bundle";
    private static final float HUB_PREVIEW_ZOOM = 14f;
    private static final LatLng SRI_LANKA_CENTER = new LatLng(7.8731, 80.7718);

    private double hubLatitude;
    private double hubLongitude;
    private String hubName;
    private String hubPhoneNumber;
    private String hubImageUrl;
    private MapView mapHubPreview;
    private GoogleMap googleMapPreview;

    private final ActivityResultLauncher<String> callPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startDirectCall(hubPhoneNumber);
                    return;
                }

                if (hubPhoneNumber == null || hubPhoneNumber.trim().isEmpty()) {
                    return;
                }

                Snackbar.make(findViewById(android.R.id.content),
                                R.string.single_hub_call_permission_denied,
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.single_hub_call_open_dialer, v -> openDialer(hubPhoneNumber))
                        .show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_hub);

        bindSelectedHubDetails();
        setupActions();
        setupMapPreview(savedInstanceState);
    }

    private void setupMapPreview(Bundle savedInstanceState) {
        mapHubPreview = findViewById(R.id.mapHubPreview);
        if (mapHubPreview == null) {
            return;
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapHubPreview.onCreate(mapViewBundle);
        mapHubPreview.getMapAsync(this);
    }

    private void bindSelectedHubDetails() {
        Intent intent = getIntent();
        hubName = getSafeExtra(intent, EXTRA_HUB_NAME, "Hub Details");
        String city = getSafeExtra(intent, EXTRA_HUB_CITY, "Sri Lanka");
        String distance = getSafeExtra(intent, EXTRA_HUB_DISTANCE, "Nearby");
        String openHours = getSafeExtra(intent, EXTRA_HUB_HOURS, "Open hours unavailable");
        hubPhoneNumber = normalizePhoneNumber(intent.getStringExtra(EXTRA_HUB_PHONE));
        hubImageUrl = normalizeImageUrl(intent.getStringExtra(EXTRA_HUB_IMAGE_URL));
        hubLatitude = intent.getDoubleExtra(EXTRA_HUB_LATITUDE, 0d);
        hubLongitude = intent.getDoubleExtra(EXTRA_HUB_LONGITUDE, 0d);

        ShapeableImageView imgHubHero = findViewById(R.id.imgHubHero);
        TextView txtSingleHubName = findViewById(R.id.txtSingleHubName);
        TextView txtHubAddressLine1 = findViewById(R.id.txtHubAddressLine1);
        TextView txtHubAddressLine2 = findViewById(R.id.txtHubAddressLine2);
        TextView txtHubWorkingHours = findViewById(R.id.txtHubWorkingHours);
        TextView txtHubWorkingDays = findViewById(R.id.txtHubWorkingDays);
        TextView txtHubStatus = findViewById(R.id.txtHubStatus);

        txtSingleHubName.setText(hubName);
        txtHubAddressLine1.setText(hubName + ", " + city);
        txtHubAddressLine2.setText("Sri Lanka");
        txtHubWorkingHours.setText(openHours);
        txtHubWorkingDays.setText("Monday - Sunday");
        txtHubStatus.setText(getString(R.string.single_hub_status_from_distance, distance));

        loadHeroImage(imgHubHero);
    }

    private void loadHeroImage(ShapeableImageView imgHubHero) {
        if (imgHubHero == null) {
            return;
        }

        if (!TextUtils.isEmpty(hubImageUrl)) {
            Glide.with(this)
                    .load(hubImageUrl)
                    .placeholder(R.drawable.hero_bg)
                    .error(R.drawable.hero_bg)
                    .centerCrop()
                    .into(imgHubHero);
            return;
        }

        imgHubHero.setImageResource(R.drawable.hero_bg);
    }

    private void setupActions() {
        ImageButton btnHubBack = findViewById(R.id.btnHubBack);
        ImageButton btnHubShare = findViewById(R.id.btnHubShare);
        MaterialButton btnHubDirections = findViewById(R.id.btnHubDirections);
        MaterialButton btnHubCall = findViewById(R.id.btnHubCall);

        btnHubBack.setOnClickListener(v -> finish());

        btnHubShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, hubName + "\n" + "https://maps.google.com/?q=" + hubLatitude + "," + hubLongitude);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.single_hub_share)));
        });

        btnHubDirections.setOnClickListener(v -> openDirections());
        btnHubCall.setOnClickListener(this::onCallHubTapped);
    }

    private void onCallHubTapped(android.view.View anchor) {
        if (hubPhoneNumber == null || hubPhoneNumber.trim().isEmpty()) {
            Snackbar.make(anchor, R.string.single_hub_call_unavailable, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startDirectCall(hubPhoneNumber);
            return;
        }

        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
    }

    private void startDirectCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(phoneNumber)));
        if (callIntent.resolveActivity(getPackageManager()) == null) {
            openDialer(phoneNumber);
            return;
        }

        try {
            startActivity(callIntent);
        } catch (SecurityException ignored) {
            openDialer(phoneNumber);
        } catch (ActivityNotFoundException ignored) {
            openDialer(phoneNumber);
        }
    }

    private void openDialer(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phoneNumber)));
        if (dialIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(dialIntent);
            return;
        }

        Snackbar.make(findViewById(android.R.id.content), R.string.single_hub_call_failed, Snackbar.LENGTH_SHORT).show();
    }

    private String normalizePhoneNumber(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        return rawPhone.trim();
    }

    private String normalizeImageUrl(String rawImageUrl) {
        if (rawImageUrl == null) {
            return "";
        }
        return rawImageUrl.trim();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMapPreview = map;

        googleMapPreview.getUiSettings().setMapToolbarEnabled(false);
        googleMapPreview.getUiSettings().setCompassEnabled(false);
        googleMapPreview.getUiSettings().setZoomControlsEnabled(false);
        googleMapPreview.getUiSettings().setMyLocationButtonEnabled(false);
        googleMapPreview.getUiSettings().setAllGesturesEnabled(false);

        if (isValidHubLocation()) {
            LatLng hubPoint = new LatLng(hubLatitude, hubLongitude);
            googleMapPreview.clear();
            googleMapPreview.addMarker(new MarkerOptions().position(hubPoint).title(hubName));
            googleMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(hubPoint, HUB_PREVIEW_ZOOM));
            return;
        }

        googleMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(SRI_LANKA_CENTER, 7f));
    }

    private boolean isValidHubLocation() {
        boolean latitudeValid = hubLatitude >= -90d && hubLatitude <= 90d;
        boolean longitudeValid = hubLongitude >= -180d && hubLongitude <= 180d;
        boolean hasExactCoordinate = Math.abs(hubLatitude) > 0.000001d || Math.abs(hubLongitude) > 0.000001d;
        return latitudeValid && longitudeValid && hasExactCoordinate;
    }

    private void openDirections() {
        String query = hubLatitude + "," + hubLongitude;
        Uri navigationUri = Uri.parse("google.navigation:q=" + query);
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigationUri);
        navigationIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(navigationIntent);
        } catch (ActivityNotFoundException ignored) {
            Uri geoUri = Uri.parse("geo:0,0?q=" + query + "(" + Uri.encode(hubName) + ")");
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            if (geoIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(geoIntent);
            }
        }
    }

    private String getSafeExtra(Intent intent, String key, String fallback) {
        String value = intent.getStringExtra(key);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapHubPreview != null) {
            mapHubPreview.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapHubPreview != null) {
            mapHubPreview.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mapHubPreview != null) {
            mapHubPreview.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mapHubPreview != null) {
            mapHubPreview.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mapHubPreview != null) {
            mapHubPreview.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapHubPreview != null) {
            mapHubPreview.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapHubPreview == null) {
            return;
        }

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapHubPreview.onSaveInstanceState(mapViewBundle);
    }
}
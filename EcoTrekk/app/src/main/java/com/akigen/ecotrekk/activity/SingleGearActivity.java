package com.akigen.ecotrekk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.akigen.ecotrekk.R;
import com.akigen.ecotrekk.api.CartApiHelper;
import com.bumptech.glide.Glide;
import com.akigen.ecotrekk.dto.CartAddRequest;
import com.akigen.ecotrekk.database.CartHelper;
import com.akigen.ecotrekk.database.FavoritesHelper;
import com.akigen.ecotrekk.manager.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleGearActivity extends AppCompatActivity {

    private ShapeableImageView imgGearHero;
    private TextView txtSingleGearName;
    private TextView txtSingleGearPrice;
    private TextView txtSingleGearCategory;
    private TextView txtSingleGearDeposit;
    private TextView txtSingleGearDescription;
    private TextView txtQtyCount;
    private ImageButton btnGearBack;
    private ImageButton btnGearWishlist;
    private com.google.android.material.button.MaterialButton btnQtyDecrease;
    private com.google.android.material.button.MaterialButton btnQtyIncrease;
    private com.google.android.material.button.MaterialButton btnAddToCart;
    private int quantity = 1;
    private boolean isFavorited = false;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    
    // API helpers
    private CartApiHelper cartApiHelper;
    private CartHelper cartHelper;
    private FavoritesHelper favoritesHelper;
    
    // Gear data
    private long gearId;
    private String gearName;
    private String gearCategory;
    private double gearPrice;
    private double gearDeposit;
    private String gearDescription;
    private String gearImageUrl;
    private int gearImageResId;
    /** Available stock; Integer.MAX_VALUE = unknown (no limit applied) */
    private int gearAvailableQty = Integer.MAX_VALUE;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_gear);

        // Initialize managers
        sessionManager = new SessionManager(this);
        favoritesHelper = FavoritesHelper.getInstance(this);
        cartHelper = CartHelper.getInstance(this);
        Long customerId = sessionManager.getCustomerId();
        cartApiHelper = new CartApiHelper(customerId);

        // Initialize FavoritesHelper with current customer ID for server sync
        if (customerId > 0) {
            favoritesHelper.initializeWithCustomerId(customerId);
        }

        initializeViews();
        loadGearData();
        setupClickListeners();
    }

    private void initializeViews() {
        imgGearHero = findViewById(R.id.imgGearHero);
        txtSingleGearName = findViewById(R.id.txtSingleGearName);
        txtSingleGearPrice = findViewById(R.id.txtSingleGearPrice);
        txtSingleGearCategory = findViewById(R.id.txtSingleGearCategory);
        txtSingleGearDeposit = findViewById(R.id.txtSingleGearDeposit);
        txtSingleGearDescription = findViewById(R.id.txtSingleGearDescription);
        txtQtyCount = findViewById(R.id.txtQtyCount);
        btnGearBack = findViewById(R.id.btnGearBack);
        btnGearWishlist = findViewById(R.id.btnGearWishlist);
        btnQtyDecrease = findViewById(R.id.btnQtyDecrease);
        btnQtyIncrease = findViewById(R.id.btnQtyIncrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
    }

    private void loadGearData() {
        Intent intent = getIntent();
        
        gearId = intent.getLongExtra("gear_id", 0);
        gearName = intent.getStringExtra("gear_name");
        gearCategory = intent.getStringExtra("gear_category");
        gearDescription = intent.getStringExtra("gear_description");
        gearImageUrl = intent.getStringExtra("gear_image_url");
        gearPrice = intent.getDoubleExtra("gear_price", 0.0);
        gearDeposit = intent.getDoubleExtra("gear_deposit", 0.0);
        gearImageResId = intent.getIntExtra("gear_image_res_id", R.drawable.ecotrekk_notfound);
        gearAvailableQty = intent.getIntExtra("gear_available_qty", Integer.MAX_VALUE);

        // Display data
        txtSingleGearName.setText(gearName);
        txtSingleGearCategory.setText(gearCategory);
        txtSingleGearDescription.setText(gearDescription);
        
        // Format and display price
        txtSingleGearPrice.setText(String.format(Locale.US, "LKR %,.0f", gearPrice));
        
        // Format and display deposit
        txtSingleGearDeposit.setText(String.format(Locale.US, "LKR %,.0f", gearDeposit));
        
        // Set image from URL when available, otherwise use drawable fallback.
        if (!TextUtils.isEmpty(gearImageUrl)) {
            Glide.with(this)
                    .load(gearImageUrl)
                    .placeholder(gearImageResId)
                    .error(gearImageResId)
                    .centerCrop()
                    .into(imgGearHero);
        } else {
            imgGearHero.setImageResource(gearImageResId);
        }
        
        // Phase 2: Observe favorites so icon updates reactively.
        observeWishlistState();
        refreshWishlistState();

        updateQuantityViews();
        applyInventoryState();
    }

    private void setupClickListeners() {
        btnGearBack.setOnClickListener(v -> finish());
        
        btnGearWishlist.setOnClickListener(v -> {
            // Perform toggle on background thread to avoid database access on main thread
            ioExecutor.execute(() -> {
                boolean wasFavorited = isFavorited;

                // Toggle favorite status with server sync (background thread safe)
                favoritesHelper.toggleFavoriteWithServerSync(
                    gearId,
                    gearName,
                    gearCategory,
                    gearPrice,
                    gearDeposit,
                    gearDescription,
                    gearImageUrl != null ? gearImageUrl : "default_image"
                );
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    // Show feedback
                    Toast.makeText(
                        SingleGearActivity.this,
                        wasFavorited ? "Removed from favorites" : "Added to favorites",
                        Toast.LENGTH_SHORT
                    ).show();
                });
            });
        });
        
        btnQtyDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityViews();
            }
        });

        btnQtyIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantityViews();
        });

        btnAddToCart.setOnClickListener(v -> {
            addToCartOnServer();
        });
    }

    private void addToCartOnServer() {
        long customerId = sessionManager.getCustomerId();
        if (customerId <= 0) {
            Toast.makeText(this, R.string.single_gear_add_to_cart_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (gearId <= 0) {
            Toast.makeText(this, R.string.single_gear_add_to_cart_invalid_gear, Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddToCart.setEnabled(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        String startDate = dateFormat.format(now);
        String endDate = dateFormat.format(calendar.getTime());
        Long hubId = getIntent().getLongExtra("hub_id", 1L);
        long startDateMillis = now.getTime();
        long endDateMillis = calendar.getTimeInMillis();

        CartAddRequest request = new CartAddRequest(
                gearId,
                quantity,
                hubId,
                startDate,
                endDate
        );

        cartApiHelper.addToCart(request, new CartApiHelper.CartCallback<>() {
            @Override
            public void onSuccess(com.akigen.ecotrekk.dto.CartItemResponse result) {
                runOnUiThread(() -> {
                    persistToLocalCart(hubId, startDateMillis, endDateMillis);
                    btnAddToCart.setEnabled(true);
                    Toast.makeText(
                            SingleGearActivity.this,
                            getString(R.string.single_gear_added_to_cart, quantity),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    persistToLocalCart(hubId, startDateMillis, endDateMillis);
                    btnAddToCart.setEnabled(true);
                    Toast.makeText(
                            SingleGearActivity.this,
                            getString(R.string.single_gear_add_to_cart_failed, message) + " (saved locally)",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void persistToLocalCart(Long hubId, long startDateMillis, long endDateMillis) {
        long safeHubId = hubId != null && hubId > 0 ? hubId : 1L;
        String safeImageUrl = !TextUtils.isEmpty(gearImageUrl) ? gearImageUrl : "";

        cartHelper.addToCart(
                gearId,
                gearName,
                gearCategory,
                quantity,
                startDateMillis,
                endDateMillis,
                gearPrice,
                gearDeposit,
                safeHubId,
                "Hub " + safeHubId,
                safeImageUrl
        );
    }

    /**
     * Update wishlist icon based on favorite status
     */
    private void applyWishlistIcon() {
        if (isFavorited) {
            btnGearWishlist.setImageResource(R.drawable.favorite_24px);
            btnGearWishlist.setContentDescription("Remove from wishlist");
        } else {
            btnGearWishlist.setImageResource(R.drawable.favorite_border_24px);
            btnGearWishlist.setContentDescription("Add to wishlist");
        }
    }

    private void refreshWishlistState() {
        ioExecutor.execute(() -> {
            boolean favoriteInDb = favoritesHelper.isFavorite(gearId);
            runOnUiThread(() -> {
                isFavorited = favoriteInDb;
                applyWishlistIcon();
            });
        });
    }

    private void observeWishlistState() {
        favoritesHelper.observeFavorites().observe(this, favorites -> {
            boolean favoriteInDb = false;
            if (favorites != null) {
                for (com.akigen.ecotrekk.database.entity.FavoriteEntity favorite : favorites) {
                    if (favorite.getGearId() == gearId) {
                        favoriteInDb = true;
                        break;
                    }
                }
            }

            if (isFavorited != favoriteInDb) {
                isFavorited = favoriteInDb;
                applyWishlistIcon();
            }
        });
    }

    private void updateQuantityViews() {
        txtQtyCount.setText(String.valueOf(quantity));
        btnQtyDecrease.setEnabled(quantity > 1);
        // Cap increase button at available inventory
        boolean canIncrease = gearAvailableQty == Integer.MAX_VALUE || quantity < gearAvailableQty;
        btnQtyIncrease.setEnabled(canIncrease);
    }

    /**
     * Show/hide Out of Stock state and disable Add to Cart when nothing is available.
     */
    private void applyInventoryState() {
        boolean outOfStock = gearAvailableQty == 0;
        btnAddToCart.setEnabled(!outOfStock);
        if (outOfStock) {
            btnAddToCart.setText(R.string.single_gear_out_of_stock);
        } else if (gearAvailableQty != Integer.MAX_VALUE) {
            // Show a small availability hint alongside the normal label
            btnAddToCart.setText(getString(R.string.single_gear_add_to_cart_with_stock, gearAvailableQty));
        } else {
            btnAddToCart.setText(R.string.single_gear_add_to_cart);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }
}
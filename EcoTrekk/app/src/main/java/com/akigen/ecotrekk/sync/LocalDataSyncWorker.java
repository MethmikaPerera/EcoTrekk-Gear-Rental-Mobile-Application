package com.akigen.ecotrekk.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.akigen.ecotrekk.api.ApiService;
import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.database.CartHelper;
import com.akigen.ecotrekk.database.FavoritesHelper;
import com.akigen.ecotrekk.database.entity.CartItemEntity;
import com.akigen.ecotrekk.database.entity.FavoriteEntity;
import com.akigen.ecotrekk.dto.CartItemResponse;
import com.akigen.ecotrekk.dto.WishlistItemResponse;
import com.akigen.ecotrekk.manager.SessionManager;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Periodic background worker that syncs cart and favorites from backend to Room.
 */
public class LocalDataSyncWorker extends Worker {

    private static final String TAG = "LocalDataSyncWorker";

    public LocalDataSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            long customerId = sessionManager.getCustomerId();
            if (customerId <= 0) {
                return Result.success();
            }

            ApiService apiService = RetrofitClient.getInstance().getApiService();

            syncCart(apiService, customerId);
            syncFavorites(apiService, customerId);

            return Result.success();
        } catch (Exception ex) {
            Log.e(TAG, "Sync failed", ex);
            return Result.retry();
        }
    }

    private void syncCart(ApiService apiService, long customerId) {
        try {
            Response<List<CartItemResponse>> response = apiService.getCartItems(customerId).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return;
            }

            List<CartItemEntity> entities = new ArrayList<>();
            for (CartItemResponse dto : response.body()) {
                long gearId = dto.getGearId() != null ? dto.getGearId() : -1L;
                if (gearId <= 0) {
                    continue;
                }

                long hubId = dto.getHubId() != null ? dto.getHubId() : 1L;
                String hubName = dto.getHubName() != null ? dto.getHubName() : ("Hub " + hubId);

                long startMillis = parseIsoDateToUtcMillis(dto.getStartDate());
                long endMillis = parseIsoDateToUtcMillis(dto.getEndDate());

                CartItemEntity entity = new CartItemEntity(
                        gearId,
                        dto.getGearName() != null ? dto.getGearName() : "Cart Item",
                        dto.getCategory() != null ? dto.getCategory() : "Other",
                        dto.getQuantity() != null ? Math.max(1, dto.getQuantity()) : 1,
                        startMillis,
                        endMillis,
                        dto.getPricePerDay() != null ? dto.getPricePerDay() : 0.0,
                        dto.getDepositAmount() != null ? dto.getDepositAmount() : 0.0,
                        hubId,
                        hubName,
                        dto.getImageUrl() != null ? dto.getImageUrl() : ""
                );
                entities.add(entity);
            }

            CartHelper.getInstance(getApplicationContext()).replaceCartItems(entities);
        } catch (Exception ex) {
            Log.w(TAG, "Cart sync skipped", ex);
        }
    }

    private void syncFavorites(ApiService apiService, long customerId) {
        try {
            Response<List<WishlistItemResponse>> response = apiService.getWishlist(customerId).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return;
            }

            List<FavoriteEntity> entities = new ArrayList<>();
            for (WishlistItemResponse dto : response.body()) {
                long gearId = dto.getGearId() != null ? dto.getGearId() : -1L;
                if (gearId <= 0) {
                    continue;
                }

                FavoriteEntity entity = new FavoriteEntity(
                        gearId,
                        dto.getGearName() != null ? dto.getGearName() : "Favorite Item",
                        dto.getCategory() != null ? dto.getCategory() : "Other",
                        dto.getPricePerDay() != null ? dto.getPricePerDay() : 0.0,
                        dto.getDepositAmount() != null ? dto.getDepositAmount() : 0.0,
                        dto.getDescription() != null ? dto.getDescription() : "",
                        dto.getImageUrl() != null ? dto.getImageUrl() : ""
                );
                entities.add(entity);
            }

            FavoritesHelper.getInstance(getApplicationContext()).replaceFavorites(entities);
        } catch (Exception ex) {
            Log.w(TAG, "Favorites sync skipped", ex);
        }
    }

    private long parseIsoDateToUtcMillis(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return 0L;
        }

        try {
            LocalDate localDate = LocalDate.parse(isoDate);
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (Exception ignored) {
            return 0L;
        }
    }
}

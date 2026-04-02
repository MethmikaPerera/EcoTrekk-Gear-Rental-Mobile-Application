package com.akigen.ecotrekk.api;

import android.util.Log;

import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.WishlistItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class for wishlist API operations
 * Handles synchronization between local and server wishlist database
 */
public class WishlistApiHelper {

    private static final String TAG = "WishlistApiHelper";
    private final ApiService apiService;
    private final Long customerId;

    public interface WishlistCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public WishlistApiHelper(Long customerId) {
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.customerId = customerId;
    }

    /**
     * Fetch all wishlist items from server
     */
    public void fetchWishlist(WishlistCallback<List<WishlistItemResponse>> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.getWishlist(customerId).enqueue(new Callback<List<WishlistItemResponse>>() {
            @Override
            public void onResponse(Call<List<WishlistItemResponse>> call, Response<List<WishlistItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch wishlist: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<WishlistItemResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching wishlist", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Add item to server wishlist
     */
    public void addToWishlist(Long gearId, WishlistCallback<WishlistItemResponse> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        if (gearId == null || gearId <= 0) {
            callback.onError("Invalid gear ID");
            return;
        }

        apiService.addToWishlist(customerId, gearId).enqueue(new Callback<WishlistItemResponse>() {
            @Override
            public void onResponse(Call<WishlistItemResponse> call, Response<WishlistItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add to wishlist: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WishlistItemResponse> call, Throwable t) {
                Log.e(TAG, "Error adding to wishlist", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Remove item from server wishlist
     */
    public void removeFromWishlist(Long gearId, WishlistCallback<Void> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        if (gearId == null || gearId <= 0) {
            callback.onError("Invalid gear ID");
            return;
        }

        apiService.removeFromWishlist(customerId, gearId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to remove from wishlist: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error removing from wishlist", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Check if item is in server wishlist
     */
    public void checkIfInWishlist(Long gearId, WishlistCallback<Boolean> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        if (gearId == null || gearId <= 0) {
            callback.onError("Invalid gear ID");
            return;
        }

        apiService.isInWishlist(customerId, gearId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to check wishlist: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error checking wishlist", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get wishlist count from server
     */
    public void getWishlistCount(WishlistCallback<Integer> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.getWishlistCount(customerId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get wishlist count: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "Error getting wishlist count", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Clear all wishlist items on server
     */
    public void clearWishlist(WishlistCallback<Void> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.clearWishlist(customerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to clear wishlist: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error clearing wishlist", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

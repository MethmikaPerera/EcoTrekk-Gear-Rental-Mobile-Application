package com.akigen.ecotrekk.api;

import android.util.Log;

import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.CartAddRequest;
import com.akigen.ecotrekk.dto.CartItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartApiHelper {

    private static final String TAG = "CartApiHelper";
    private final ApiService apiService;
    private final Long customerId;

    public interface CartCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    public CartApiHelper(Long customerId) {
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.customerId = customerId;
    }

    public void fetchCartItems(CartCallback<List<CartItemResponse>> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.getCartItems(customerId).enqueue(new Callback<List<CartItemResponse>>() {
            @Override
            public void onResponse(Call<List<CartItemResponse>> call, Response<List<CartItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load cart items: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CartItemResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading cart items", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void addToCart(CartAddRequest request, CartCallback<CartItemResponse> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.addToCart(customerId, request).enqueue(new Callback<CartItemResponse>() {
            @Override
            public void onResponse(Call<CartItemResponse> call, Response<CartItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add to cart: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CartItemResponse> call, Throwable t) {
                Log.e(TAG, "Error adding to cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void removeCartItem(Long cartItemId, CartCallback<Void> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        if (cartItemId == null || cartItemId <= 0) {
            callback.onError("Invalid cart item ID");
            return;
        }

        apiService.removeCartItem(customerId, cartItemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to remove cart item: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error removing cart item", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

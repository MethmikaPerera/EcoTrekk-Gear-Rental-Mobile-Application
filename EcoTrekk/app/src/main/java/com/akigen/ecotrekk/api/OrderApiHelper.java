package com.akigen.ecotrekk.api;

import android.util.Log;

import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.OrderHistoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderApiHelper {

    private static final String TAG = "OrderApiHelper";
    private final ApiService apiService;
    private final Long customerId;

    public interface OrderCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public OrderApiHelper(Long customerId) {
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.customerId = customerId;
    }

    public void fetchCustomerOrderHistory(OrderCallback<List<OrderHistoryResponse>> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.getCustomerOrderHistory(customerId).enqueue(new Callback<List<OrderHistoryResponse>>() {
            @Override
            public void onResponse(Call<List<OrderHistoryResponse>> call, Response<List<OrderHistoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load order history: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<OrderHistoryResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading order history", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

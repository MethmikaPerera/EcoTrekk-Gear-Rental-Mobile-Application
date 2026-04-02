package com.akigen.ecotrekk.api;

import android.util.Log;

import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.CashOrderResponse;
import com.akigen.ecotrekk.dto.CreateOrderRequest;
import com.akigen.ecotrekk.dto.StripePaymentIntentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripeApiHelper {

    private static final String TAG = "StripeApiHelper";
    private final ApiService apiService;
    private final Long customerId;

    public interface StripeCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    public StripeApiHelper(Long customerId) {
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.customerId = customerId;
    }

    public void createPaymentIntent(StripeCallback<StripePaymentIntentResponse> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        apiService.createStripePaymentIntent(customerId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<StripePaymentIntentResponse> call, Response<StripePaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to initialize payment: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StripePaymentIntentResponse> call, Throwable t) {
                Log.e(TAG, "Error creating payment intent", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createCashOrder(String collectionHubName, long startDateUtc, long endDateUtc, StripeCallback<CashOrderResponse> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        CreateOrderRequest request = new CreateOrderRequest(collectionHubName, startDateUtc, endDateUtc);
        apiService.createCashOrder(customerId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<CashOrderResponse> call, Response<CashOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to place cash order: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CashOrderResponse> call, Throwable t) {
                Log.e(TAG, "Error creating cash order", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createCardOrder(String collectionHubName, long startDateUtc, long endDateUtc, StripeCallback<CashOrderResponse> callback) {
        if (customerId == null || customerId <= 0) {
            callback.onError("Invalid customer ID");
            return;
        }

        CreateOrderRequest request = new CreateOrderRequest(collectionHubName, startDateUtc, endDateUtc);
        apiService.createCardOrder(customerId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<CashOrderResponse> call, Response<CashOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to place card order: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CashOrderResponse> call, Throwable t) {
                Log.e(TAG, "Error creating card order", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

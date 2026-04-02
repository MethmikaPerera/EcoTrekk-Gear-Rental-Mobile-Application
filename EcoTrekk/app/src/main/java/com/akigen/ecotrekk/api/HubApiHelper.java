package com.akigen.ecotrekk.api;

import android.util.Log;

import com.akigen.ecotrekk.client.RetrofitClient;
import com.akigen.ecotrekk.dto.PublicHubResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HubApiHelper {

    private static final String TAG = "HubApiHelper";
    private final ApiService apiService;

    public interface HubCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    public HubApiHelper() {
        this.apiService = RetrofitClient.getInstance().getApiService();
    }

    public void fetchPublicHubs(HubCallback<List<PublicHubResponse>> callback) {
        apiService.getPublicHubs().enqueue(new Callback<List<PublicHubResponse>>() {
            @Override
            public void onResponse(Call<List<PublicHubResponse>> call, Response<List<PublicHubResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load hubs: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PublicHubResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading hubs", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

package com.akigen.ecotrekk.api;

import com.akigen.ecotrekk.dto.CustomerAuthRequest;
import com.akigen.ecotrekk.dto.CustomerAuthResponse;
import com.akigen.ecotrekk.dto.CashOrderResponse;
import com.akigen.ecotrekk.dto.CartAddRequest;
import com.akigen.ecotrekk.dto.CartItemResponse;
import com.akigen.ecotrekk.dto.CreateOrderRequest;
import com.akigen.ecotrekk.dto.CustomerProfileUpdateRequest;
import com.akigen.ecotrekk.dto.CustomerRegisterRequest;
import com.akigen.ecotrekk.dto.CustomerRegisterResponse;
import com.akigen.ecotrekk.dto.DeviceFcmTokenRequest;
import com.akigen.ecotrekk.dto.DeviceFcmTokenResponse;
import com.akigen.ecotrekk.dto.GearCategoryOptionResponse;
import com.akigen.ecotrekk.dto.PublicFeaturedGearResponse;
import com.akigen.ecotrekk.dto.PublicHubResponse;
import com.akigen.ecotrekk.dto.OrderHistoryResponse;
import com.akigen.ecotrekk.dto.StripePaymentIntentResponse;
import com.akigen.ecotrekk.dto.WishlistItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Part;
import retrofit2.http.Query;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public interface ApiService {

        /** Login with Firebase ID token (existing users) */
        @POST("api/auth/customer/firebase")
        Call<CustomerAuthResponse> customerFirebaseLogin(@Body CustomerAuthRequest request);

        /** Backend-driven registration — returns a Firebase custom token */
        @POST("api/auth/customer/register")
        Call<CustomerRegisterResponse> customerRegister(@Body CustomerRegisterRequest request);

        /** Public categories for home explore section */
        @GET("api/public/gears/categories")
        Call<List<GearCategoryOptionResponse>> getPublicGearCategories();

        /** Public featured gears for home section */
        @GET("api/public/gears/featured")
        Call<List<PublicFeaturedGearResponse>> getPublicFeaturedGears(@Query("limit") int limit);

        /** Public gears list for customer app */
        @GET("api/public/gears")
        Call<List<PublicFeaturedGearResponse>> getPublicGears();

        /** Public hubs list for customer app */
        @GET("api/public/hubs")
        Call<List<PublicHubResponse>> getPublicHubs();

        /** Get customer order history */
        @GET("api/customer/orders/{customerId}")
        Call<List<OrderHistoryResponse>> getCustomerOrderHistory(@Path("customerId") Long customerId);

        /** Get all wishlist items for customer */
        @GET("api/customer/wishlist/{customerId}")
        Call<List<WishlistItemResponse>> getWishlist(@Path("customerId") Long customerId);

        /** Get all cart items for customer */
        @GET("api/customer/cart/{customerId}/items")
        Call<List<CartItemResponse>> getCartItems(@Path("customerId") Long customerId);

        /** Add an item to cart */
        @POST("api/customer/cart/{customerId}/items")
        Call<CartItemResponse> addToCart(
                        @Path("customerId") Long customerId,
                        @Body CartAddRequest request);

        /** Remove cart item */
        @DELETE("api/customer/cart/{customerId}/items/{cartItemId}")
        Call<Void> removeCartItem(
                        @Path("customerId") Long customerId,
                        @Path("cartItemId") Long cartItemId);

        /** Create Stripe payment intent for checkout */
        @POST("api/customer/payments/{customerId}/intent")
        Call<StripePaymentIntentResponse> createStripePaymentIntent(@Path("customerId") Long customerId);

        /** Place cash-on-collection order */
        @POST("api/customer/payments/{customerId}/cash-order")
        Call<CashOrderResponse> createCashOrder(
                        @Path("customerId") Long customerId,
                        @Body CreateOrderRequest request);

        /** Place card-paid order */
        @POST("api/customer/payments/{customerId}/card-order")
        Call<CashOrderResponse> createCardOrder(
                        @Path("customerId") Long customerId,
                        @Body CreateOrderRequest request);

        /** Add gear to wishlist */
        @POST("api/customer/wishlist/{customerId}/add/{gearId}")
        Call<WishlistItemResponse> addToWishlist(
                        @Path("customerId") Long customerId,
                        @Path("gearId") Long gearId);

        /** Remove gear from wishlist */
        @DELETE("api/customer/wishlist/{customerId}/{gearId}")
        Call<Void> removeFromWishlist(
                        @Path("customerId") Long customerId,
                        @Path("gearId") Long gearId);

        /** Check if gear is in wishlist */
        @GET("api/customer/wishlist/{customerId}/check/{gearId}")
        Call<Boolean> isInWishlist(
                        @Path("customerId") Long customerId,
                        @Path("gearId") Long gearId);

        /** Get wishlist count for customer */
        @GET("api/customer/wishlist/{customerId}/count")
        Call<Integer> getWishlistCount(@Path("customerId") Long customerId);

        /** Clear all wishlist items for customer */
        @DELETE("api/customer/wishlist/{customerId}/clear")
        Call<Void> clearWishlist(@Path("customerId") Long customerId);

        /** Update customer profile details */
        @POST("api/auth/customer/profile")
        Call<CustomerAuthResponse> updateCustomerProfile(@Body CustomerProfileUpdateRequest request);

        /** Register or update customer device token for FCM */
        @POST("api/auth/customer/{customerId}/device-token")
        Call<DeviceFcmTokenResponse> registerDeviceToken(
                        @Path("customerId") Long customerId,
                        @Body DeviceFcmTokenRequest request);

        /** Deactivate all customer device tokens on logout */
        @POST("api/auth/customer/{customerId}/device-tokens/logout")
        Call<Void> deactivateAllDeviceTokens(@Path("customerId") Long customerId);

        /** Update customer profile image via backend Cloudinary upload */
        @Multipart
        @POST("api/auth/customer/profile-image")
        Call<CustomerAuthResponse> updateCustomerProfileImage(
                        @Part MultipartBody.Part image,
                        @Part("idToken") RequestBody idToken);
}

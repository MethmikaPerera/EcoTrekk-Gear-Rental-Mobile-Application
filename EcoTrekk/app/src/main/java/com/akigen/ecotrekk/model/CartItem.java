package com.akigen.ecotrekk.model;

import lombok.Getter;

@Getter
public class CartItem {
    private final long cartItemId;
    private final long gearId;
    private final long hubId;
    private final String name;
    private final String category;
    private final String imageUrl;
    private final double depositAmount;
    private int quantity;
    private int rentalDays;
    private final double perDayPrice;
    private final int imageResId;
    /** Available inventory units; Integer.MAX_VALUE means "unknown / no cap" */
    private final int availableQty;

    public CartItem(String name, String category, int quantity, int rentalDays, double perDayPrice, int imageResId) {
        this(-1L, -1L, -1L, name, category, quantity, rentalDays, perDayPrice, 0.0, null, imageResId, Integer.MAX_VALUE);
    }

    public CartItem(long gearId, long hubId, String name, String category, int quantity, int rentalDays, double perDayPrice, double depositAmount, int imageResId) {
        this(-1L, gearId, hubId, name, category, quantity, rentalDays, perDayPrice, depositAmount, null, imageResId, Integer.MAX_VALUE);
    }

    public CartItem(long gearId, long hubId, String name, String category, int quantity, int rentalDays, double perDayPrice, double depositAmount, String imageUrl, int imageResId) {
        this(-1L, gearId, hubId, name, category, quantity, rentalDays, perDayPrice, depositAmount, imageUrl, imageResId, Integer.MAX_VALUE);
    }

    public CartItem(long cartItemId, long gearId, long hubId, String name, String category, int quantity, int rentalDays, double perDayPrice, double depositAmount, String imageUrl, int imageResId) {
        this(cartItemId, gearId, hubId, name, category, quantity, rentalDays, perDayPrice, depositAmount, imageUrl, imageResId, Integer.MAX_VALUE);
    }

    public CartItem(long cartItemId, long gearId, long hubId, String name, String category, int quantity, int rentalDays, double perDayPrice, double depositAmount, String imageUrl, int imageResId, int availableQty) {
        this.cartItemId = cartItemId;
        this.gearId = gearId;
        this.hubId = hubId;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.depositAmount = depositAmount;
        this.quantity = quantity;
        this.rentalDays = rentalDays;
        this.perDayPrice = perDayPrice;
        this.imageResId = imageResId;
        this.availableQty = availableQty;
    }

    public double getLineTotal() {
        return quantity * rentalDays * perDayPrice;
    }

    public void increaseQuantity() {
        quantity++;
    }

    public void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
        }
    }

    public void increaseRentalDays() {
        rentalDays++;
    }

    public void decreaseRentalDays() {
        if (rentalDays > 1) {
            rentalDays--;
        }
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = Math.max(1, rentalDays);
    }
}

package com.akigen.ecotrekk.model;

import lombok.Getter;

@Getter
public class GearProduct {
    private final long id;
    private final String name;
    private final String category;
    private final String description;
    private final double perDayPrice;
    private final double depositAmount;
    private final String imageUrl;
    private final int imageResId;
    /** Available inventory units; Integer.MAX_VALUE means "unknown / no cap" */
    private final int availableQty;

    public GearProduct(long id, String name, String category, String description, double perDayPrice, double depositAmount, String imageUrl, int imageResId) {
        this(id, name, category, description, perDayPrice, depositAmount, imageUrl, imageResId, Integer.MAX_VALUE);
    }

    public GearProduct(long id, String name, String category, String description, double perDayPrice, double depositAmount, String imageUrl, int imageResId, int availableQty) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.perDayPrice = perDayPrice;
        this.depositAmount = depositAmount;
        this.imageUrl = imageUrl;
        this.imageResId = imageResId;
        this.availableQty = availableQty;
    }

}

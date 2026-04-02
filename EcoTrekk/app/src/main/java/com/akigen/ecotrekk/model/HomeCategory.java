package com.akigen.ecotrekk.model;

import lombok.Getter;

@Getter
public class HomeCategory {
    private final String name;
    private final String itemCountLabel;
    private final String imageUrl;
    private final int imageResId;

    public HomeCategory(String name, String itemCountLabel, int imageResId) {
        this(name, itemCountLabel, null, imageResId);
    }

    public HomeCategory(String name, String itemCountLabel, String imageUrl, int imageResId) {
        this.name = name;
        this.itemCountLabel = itemCountLabel;
        this.imageUrl = imageUrl;
        this.imageResId = imageResId;
    }
}

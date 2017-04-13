package com.habitrpg.android.habitica.models;

import java.util.List;

public class Shop {

    public static final String MARKET = "market";
    public static final String QUEST_SHOP = "questShop";
    public static final String TIME_TRAVELERS_SHOP = "timeTravelersShop";
    public static final String SEASONAL_SHOP = "seasonalShop";
    public String identifier;
    public String text;
    public String notes;
    public String imageName;

    public List<ShopCategory> categories;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public List<ShopCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ShopCategory> categories) {
        this.categories = categories;
    }
}

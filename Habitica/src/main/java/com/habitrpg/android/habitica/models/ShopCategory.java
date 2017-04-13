package com.habitrpg.android.habitica.models;

import java.util.List;

public class ShopCategory {

    public String identifier;
    public String text;
    public String notes;
    public Boolean purchaseAll;

    public List<ShopItem> items;

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

    public Boolean getPurchaseAll() {
        return purchaseAll;
    }

    public void setPurchaseAll(Boolean purchaseAll) {
        this.purchaseAll = purchaseAll;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public void setItems(List<ShopItem> items) {
        this.items = items;
    }
}

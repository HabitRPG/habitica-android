package com.habitrpg.android.habitica.models.inventory;

import androidx.annotation.Nullable;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Customization extends RealmObject {

    @PrimaryKey
    private String id;
    private String identifier, category, type, notes, customizationSet, customizationSetName, text;
    private boolean purchased, isBuyable;
    private Integer price, setPrice;
    private Date availableFrom, availableUntil;

    private void updateID() {
        this.id = this.identifier + "_" + this.type + "_" + this.category;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        this.updateID();
    }

    @Nullable
    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.updateID();
    }

    @Nullable
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
        this.updateID();
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCustomizationSet() {
        return this.customizationSet;
    }

    public void setCustomizationSet(String customizationSet) {
        this.customizationSet = customizationSet;
    }

    public String getCustomizationSetName() {
        return this.customizationSetName;
    }

    public void setCustomizationSetName(@Nullable String customizationSetName) {
        this.customizationSetName = customizationSetName;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean getPurchasable() {
        Date today = new Date();
        if (this.availableFrom != null && !this.availableFrom.before(today)) {
            //Not released yet
            return false;
        }

        if (this.availableUntil != null && !this.availableUntil.after(today)) {
            //Discontinued
            return false;
        }

        return true;
    }

    public boolean getPurchased() {
        return this.purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public Integer getPrice() {
        return this.price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSetPrice() {
        return this.setPrice;
    }

    public void setSetPrice(Integer setPrice) {
        this.setPrice = setPrice;
    }

    public Date getAvailableFrom() {
        return this.availableFrom;
    }

    public void setAvailableFrom(Date availableFrom) {
        this.availableFrom = availableFrom;
    }

    public Date getAvailableUntil() {
        return this.availableUntil;
    }

    public void setAvailableUntil(Date availableUntil) {
        this.availableUntil = availableUntil;
    }

    public String getImageName(String userSize, String hairColor) {

        switch (this.type) {
            case "skin":
                return "skin_" + this.identifier;
            case "shirt":
                return userSize + "_shirt_" + this.identifier;
            case "hair":
                if (this.identifier.equals("0")) {
                    return "head_0";
                }

                switch (this.category) {
                    case "color":
                        return "hair_bangs_1_" + this.identifier;
                    case "flower":
                        return "hair_flower_" + this.identifier;
                    default:
                        return "hair_" + this.category + "_" + this.identifier + "_" + hairColor;
                }
            case "background":
                return "background_" + this.identifier;
            case "chair":
                return "chair_" + identifier;
        }
        return "";
    }

    public boolean isUsable() {
        return this.price == null || this.price == 0 || this.purchased;
    }

    public String getPath() {
        String path = this.type;

        if (this.category != null) {
            path = path + "." + this.category;
        }

        path = path + "." + this.identifier;

        return path;

    }

    public boolean getIsBuyable() {
        return isBuyable;
    }

    public void setIsBuyable(boolean buyable) {
        isBuyable = buyable;
    }
}

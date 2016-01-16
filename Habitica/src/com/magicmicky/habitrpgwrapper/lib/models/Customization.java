package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

@Table(databaseName = HabitDatabase.NAME)
public class Customization extends BaseModel {

    @Column
    @PrimaryKey
    private String id;

    @Column
    private String identifier, category, type, notes, customizationSet, text;

    @Column
    private boolean purchased;

    @Column
    private Integer price, setPrice;

    @Column
    private Date availableFrom, availableUntil;

    public void updateID() {
        this.id = this.identifier + "_" + this.type + "_" + this.category;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        this.updateID();
    }

    public void setType(String type) {
        this.type = type;
        this.updateID();
    }

    public void setCategory(String category) {
        this.category = category;
        this.updateID();
    }

    public void setId(String id) {this.id = id;}
    public void setNotes(String notes) {this.notes = notes;}
    public void setCustomizationSet(String customizationSet) {this.customizationSet = customizationSet;}
    public void setText(String text) {this.text = text;}
    public void setPurchased(boolean purchased) {this.purchased = purchased;}
    public void setPrice(Integer price) {this.price = price;}
    public void setSetPrice(Integer setPrice) {this.setPrice = setPrice; }
    public void setAvailableUntil(Date availableUntil) {this.availableUntil = availableUntil; }
    public void setAvailableFrom(Date availableFrom) {this.availableFrom = availableFrom; }

    public String getId() { return this.id; }
    public String getIdentifier() { return this.identifier; }
    public String getCategory() { return this.category; }
    public String getType() { return this.type; }
    public String getNotes() { return this.notes; }
    public String getCustomizationSet() { return this.customizationSet; }
    public String getText() { return this.text; }

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
    public boolean getPurchased() { return this.purchased; }
    public Integer getPrice() { return this.price; }
    public Integer getSetPrice() { return this.setPrice; }
    public Date getAvailableFrom() { return this.availableFrom; }
    public Date getAvailableUntil() { return this.availableUntil; }

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
}

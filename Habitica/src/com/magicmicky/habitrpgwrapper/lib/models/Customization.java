package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by viirus on 13/01/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Customization extends BaseModel {

    @Column
    @PrimaryKey
    private String id;

    @Column
    private String identifier, group, type, notes, set, text;

    @Column
    private boolean purchasable, purchased;

    @Column
    private Integer price;

    public void updateID() {
        this.id = this.identifier + "_" + this.type + "_" + this.group;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        this.updateID();
    }

    public void setType(String type) {
        this.type = type;
        this.updateID();
    }

    public void setGroup(String group) {
        this.group = group;
        this.updateID();
    }

    public void setId(String id) {this.id = id;}
    public void setNotes(String notes) {this.notes = notes;}
    public void setSet(String set) {this.set = set;}
    public void setText(String text) {this.text = text;}
    public void setPurchasable(boolean purchasable) {this.purchasable = purchasable;}
    public void setPurchased(boolean purchased) {this.purchased = purchased;}
    public void setPrice(Integer price) {this.price = price;}

    public String getId() { return this.id; }
    public String getIdentifier() { return this.identifier; }
    public String getGroup() { return this.group; }
    public String getType() { return this.type; }
    public String getNotes() { return this.notes; }
    public String getSet() { return this.set; }
    public String getText() { return this.text; }
    public boolean getPurchasable() { return this.purchasable; }
    public boolean getPurchased() { return this.purchased; }
    public Integer getPrice() { return this.price; }

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

                switch (this.group) {
                    case "color":
                        return "hair_bangs_1_" + this.identifier;
                    case "flower":
                        return "hair_flower_" + this.identifier;
                    default:
                        return "hair_" + this.group + "_" + this.identifier + "_" + hairColor;
                }
        }
         return "";
    }

    public boolean isUsable() {
        return this.price == null || this.price == 0 || this.purchased;
    }

}

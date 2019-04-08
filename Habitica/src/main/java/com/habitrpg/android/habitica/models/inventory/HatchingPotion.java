package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class HatchingPotion extends RealmObject implements Item {

    @PrimaryKey
    String key;
    String text, notes;
    Integer value;
    Boolean limited, premium;

    public Boolean getLimited() {
        return limited;
    }

    public void setLimited(Boolean limited) {
        this.limited = limited;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    @Override
    public String getType() {
        return "hatchingPotions";
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

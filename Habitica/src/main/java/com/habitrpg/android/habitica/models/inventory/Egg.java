package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Egg extends RealmObject implements Item {

    @PrimaryKey
    String key;
    String text, notes;
    Integer value;
    String adjective, mountText;

    Integer stableOwned, stableTotal;

    public String getAdjective() {
        return adjective;
    }

    public void setAdjective(String adjective) {
        this.adjective = adjective;
    }

    public String getMountText() {
        return mountText;
    }

    public void setMountText(String mountText) {
        this.mountText = mountText;
    }

    public Integer getStableOwned() {
        if (stableOwned == null) {
            stableOwned = 0;
        }
        return stableOwned;
    }

    public void setStableOwned(Integer stableOwned) {
        this.stableOwned = stableOwned;
    }

    public Integer getStableTotal() {
        return stableTotal;
    }

    public void setStableTotal(Integer stableTotal) {
        this.stableTotal = stableTotal;
    }

    public String getType() {
        return "eggs";
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

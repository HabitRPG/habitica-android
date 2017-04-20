package com.habitrpg.android.habitica.models.inventory;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

public abstract class Item extends BaseModel {

    @Column
    @PrimaryKey
    String key;

    @Column
    String text, notes;

    @Column
    Integer value, owned;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getOwned() {
        if (owned == null) {
            return 0;
        }
        return owned;
    }

    public void setOwned(Integer owned) {
        this.owned = owned;
    }

    public abstract String getType();
}

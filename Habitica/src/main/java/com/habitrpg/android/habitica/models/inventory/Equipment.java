package com.habitrpg.android.habitica.models.inventory;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Equipment extends RealmObject {

    public double value;
    public String type;
    @PrimaryKey
    public String key;
    public String klass;
    public String specialClass;
    public String index;
    public String text;
    public String notes;
    public float con, str, per;
    @SerializedName("int")
    public float _int;
    public Boolean owned;

    public Equipment() {
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public String getSpecialClass() {
        return specialClass;
    }

    public void setSpecialClass(String specialClass) {
        this.specialClass = specialClass;
    }


    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
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

    public float getCon() {
        return con;
    }

    public void setCon(float con) {
        this.con = con;
    }

    public float getStr() {
        return str;
    }

    public void setStr(float str) {
        this.str = str;
    }

    public float getPer() {
        return per;
    }

    public void setPer(float per) {
        this.per = per;
    }

    public float get_int() {
        return _int;
    }

    public void set_int(float _int) {
        this._int = _int;
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }
}

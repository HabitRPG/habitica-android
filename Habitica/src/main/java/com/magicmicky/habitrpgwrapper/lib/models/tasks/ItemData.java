package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Negue on 13.07.2015.
 */
@Table(databaseName = HabitDatabase.NAME)
public class ItemData extends BaseModel {
    @Column
    public double value;
    @Column
    public String type;
    @PrimaryKey
    @Column
    public String key;

    @Column
    public String klass;

    @Column
    public String specialClass;

    @Column(name = "_index")
    public String index;
    @Column
    public String text;
    @Column
    public String notes;
    @Column
    public float con, str, per;
    @Column
    @SerializedName("int")
    public float _int;
    @Column
    public Boolean owned;

    public ItemData() {
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

package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by MagicMicky on 10/06/2014.
 */

@Table(databaseName = HabitDatabase.NAME)
public class BasicStats extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String id;

    @Column
    public Float con, str, per;

    @Column
    @SerializedName("int")
    public Float _int;

    public Float getCon() {
        return con;
    }

    public void setCon(Float con) {
        this.con = con;
    }

    public Float getStr() {
        return str;
    }

    public void setStr(Float str) {
        this.str = str;
    }

    public Float getPer() {
        return per;
    }

    public void setPer(Float per) {
        this.per = per;
    }

    public Float get_int() {
        return _int;
    }

    public void set_int(Float _int) {
        this._int = _int;
    }

    public void merge(BasicStats stats) {
        if (stats == null) {
            return;
        }
        this.con = stats.con != null ? stats.con : this.con;
        this.str = stats.str != null ? stats.str : this.str;
        this.per = stats.per != null ? stats.per : this.per;
        this._int = stats._int != null ? stats._int : this._int;
    }
}


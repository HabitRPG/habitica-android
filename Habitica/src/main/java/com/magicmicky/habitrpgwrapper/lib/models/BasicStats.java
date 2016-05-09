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
    public float con, str, per;

    @Column
    @SerializedName("int")
    public float _int;

    public BasicStats() {
        this(0,0,0,0);
    }
    public BasicStats(int con, int str, int per, int _int) {
        this.con = con;
        this.str = str;
        this.per = per;
        this._int = _int;
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
}


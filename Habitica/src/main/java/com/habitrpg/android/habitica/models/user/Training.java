package com.habitrpg.android.habitica.models.user;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Training extends RealmObject {

    @PrimaryKey
    private String userId;

    Stats stats;
    public Float con, str, per;
    @SerializedName("int")
    public Float _int;

    public Float getCon() {
        return con != null ? con : Float.valueOf(0);
    }

    public void setCon(Float con) {
        this.con = con;
    }

    public Float getStr() {
        return str != null ? str : Float.valueOf(0);
    }

    public void setStr(Float str) {
        this.str = str;
    }

    public Float getPer() {
        return per != null ? per : Float.valueOf(0);
    }

    public void setPer(Float per) {
        this.per = per;
    }

    public Float get_int() {
        return _int != null ? _int : Float.valueOf(0);
    }

    public void set_int(Float _int) {
        this._int = _int;
    }

    public void merge(Training stats) {
        if (stats == null) {
            return;
        }
        this.con = stats.con != null ? stats.con : this.con;
        this.str = stats.str != null ? stats.str : this.str;
        this.per = stats.per != null ? stats.per : this.per;
        this._int = stats._int != null ? stats._int : this._int;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}


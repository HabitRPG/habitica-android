package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MagicMicky on 10/06/2014.
 */
public class BasicStats {
    private float con, str, per;
    @SerializedName("int")
    private float _int;

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


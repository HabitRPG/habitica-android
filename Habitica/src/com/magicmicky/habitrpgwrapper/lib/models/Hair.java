package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class Hair extends BaseModel {

    @Column
    @PrimaryKey
    public String userId;

    @Column
    private int mustache,beard, bangs,base;

    @Column
    private String color;

    public Hair() {
    }
    public Hair(int mustache, int beard, int bangs, int base, String color) {
        this.mustache = mustache;
        this.beard = beard;
        this.bangs = bangs;
        this.base = base;
        this.color = color;
    }

    public int getMustache() {
        return mustache;
    }

    public void setMustache(int mustache) {
        this.mustache = mustache;
    }

    public int getBeard() {
        return beard;
    }

    public void setBeard(int beard) {
        this.beard = beard;
    }

    public int getBangs() {
        return bangs;
    }

    public void setBangs(int bangs) {
        this.bangs = bangs;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

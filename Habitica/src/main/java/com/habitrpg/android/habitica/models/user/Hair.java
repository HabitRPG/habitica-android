package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class Hair extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    public String userId;

    @Column
    private int mustache, beard, bangs, base, flower;

    @Column
    private String color;

    public Hair() {
    }

    public Hair(int mustache, int beard, int bangs, int base, String color, int flower) {
        this.mustache = mustache;
        this.beard = beard;
        this.bangs = bangs;
        this.base = base;
        this.color = color;
        this.flower = flower;
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

    public int getFlower() {
        return flower;
    }

    public void setFlower(int flower) {
        this.flower = flower;
    }

    public boolean isAvailable(int hairId) {
        return hairId > 0;
    }
}

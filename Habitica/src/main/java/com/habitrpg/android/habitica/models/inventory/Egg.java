package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

@Table(databaseName = HabitDatabase.NAME)
public class Egg extends Item {

    @Column
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

    @Override
    public String getType() {
        return "eggs";
    }
}

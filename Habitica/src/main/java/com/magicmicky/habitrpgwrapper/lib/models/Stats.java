package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME, allFields = true)
public class Stats extends PlayerMinStats{

    @Column
    private Integer toNextLevel, maxHealth, maxMP;


    public Integer getToNextLevel() {
        return toNextLevel != null ? toNextLevel : Integer.valueOf(0);
    }

    public void setToNextLevel(Integer toNextLevel) {
        if (toNextLevel != 0) {
            this.toNextLevel = toNextLevel;
        }
    }

    public Integer getMaxHealth() {
        return maxHealth != null ? maxHealth : Integer.valueOf(0);
    }

    public void setMaxHealth(Integer maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Integer getMaxMP() {
        return maxMP != null ? maxMP : Integer.valueOf(0);
    }

    public void setMaxMP(Integer maxMP) {
        if (maxMP != 0) {
            this.maxMP = maxMP;
        }
    }

    public void merge(Stats stats) {
        if (stats == null) {
            return;
        }
        super.merge(stats);
        this.toNextLevel = stats.toNextLevel != null ? stats.toNextLevel : this.toNextLevel;
        this.maxHealth = stats.maxHealth != null ? stats.maxHealth : this.maxHealth;
        this.maxMP = stats.maxMP != null ? stats.maxMP : this.maxMP;
    }
}

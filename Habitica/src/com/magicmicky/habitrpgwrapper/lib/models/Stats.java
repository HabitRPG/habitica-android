package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME, allFields = true)
public class Stats extends PlayerMinStats {

    @Column
    private int toNextLevel;//xp needed to be earned

    @Column
    private int maxHealth, maxMP;


    public int getToNextLevel() {
        return toNextLevel;
    }

    public void setToNextLevel(int toNextLevel) {
        this.toNextLevel = toNextLevel;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMaxMP() {
        return maxMP;
    }

    public void setMaxMP(int maxMP) {
        this.maxMP = maxMP;
    }


}

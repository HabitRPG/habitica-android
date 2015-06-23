package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by MagicMicky on 16/03/14.
 */
public class Stats extends PlayerMinStats{
    private int toNextLevel;//xp needed to be earned
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

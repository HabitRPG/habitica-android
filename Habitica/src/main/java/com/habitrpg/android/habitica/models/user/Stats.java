package com.habitrpg.android.habitica.models.user;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.HabitRpgClass;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Stats extends RealmObject {

    @PrimaryKey
    private String userId;

    User user;
    public Float con, str, per;
    @SerializedName("int")
    public Float _int;
    public Training training;
    public Buffs buffs;
    public Integer points, lvl;
    @SerializedName("class")
    public String habitClass;
    public Double gp, exp, mp, hp;
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
        return maxHealth != null ? maxHealth : Integer.valueOf(50);
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



    public String getTranslatedClassName(Context context) {
        switch (habitClass) {
            case "healer":
                return context.getString(R.string.healer);
            case "rogue":
                return context.getString(R.string.rogue);
            case "warrior":
                return context.getString(R.string.warrior);
            case "wizard":
                return context.getString(R.string.mage);
            default:
                return context.getString(R.string.warrior);
        }
    }

    public void merge(Stats stats) {
        if (stats == null) {
            return;
        }
        this.con = stats.con != null ? stats.con : this.con;
        this.str = stats.str != null ? stats.str : this.str;
        this.per = stats.per != null ? stats.per : this.per;
        this._int = stats._int != null ? stats._int : this._int;
        this.training.merge(stats.training);
        this.buffs.merge(stats.buffs);
        this.points = stats.points != null ? stats.points : this.points;
        this.lvl = stats.lvl != null ? stats.lvl : this.lvl;
        this.habitClass = stats.habitClass != null ? stats.habitClass : this.habitClass;
        this.gp = stats.gp != null ? stats.gp : this.gp;
        this.exp = stats.exp != null ? stats.exp : this.exp;
        this.hp = stats.hp != null ? stats.hp : this.hp;
        this.mp = stats.mp != null ? stats.mp : this.mp;
        this.toNextLevel = stats.toNextLevel != null ? stats.toNextLevel : this.toNextLevel;
        this.maxHealth = stats.maxHealth != null ? stats.maxHealth : this.maxHealth;
        this.maxMP = stats.maxMP != null ? stats.maxMP : this.maxMP;
    }

    public String getHabitClass() {
        return habitClass;
    }

    public void setHabitClass(String habitClass) {
        this.habitClass = habitClass;
    }

    public Double getGp() {
        return gp;
    }

    public Double getExp() {
        return exp;
    }

    public Double getHp() {
        return hp;
    }

    public Double getMp() {
        return mp;
    }

    public Integer getLvl() {
        return lvl;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public void setGp(double gp) {
        this.gp = gp;
    }

    public void setMp(double mp) {
        this.mp = mp;
    }

    public void setLvl(Integer lvl) {
        this.lvl = lvl;
    }

    public Buffs getBuffs() {
        return buffs;
    }

    public Float getStr() {
        return str;
    }

    public Float get_int() {
        return _int;
    }

    public Float getCon() {
        return con;
    }

    public Float getPer() {
        return per;
    }

    public void setHabitClass(HabitRpgClass habitRpgClass) {
        habitClass = habitRpgClass.toString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (buffs != null && !buffs.isManaged()) {
            buffs.setUserId(userId);
        }
        if (training != null && !training.isManaged()) {
            training.setUserId(userId);
        }
    }
}

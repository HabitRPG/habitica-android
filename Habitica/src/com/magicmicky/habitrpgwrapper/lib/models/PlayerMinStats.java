package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by MagicMicky on 12/06/2014.
 */

@Table(databaseName = HabitDatabase.NAME, allFields = true)
public class PlayerMinStats extends BasicStats {

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "trainingstats_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    public BasicStats training;//stats.training

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "buffs_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    public Buffs buffs;//stats.buffs

    @Column
    public int points, lvl;

    @Column
    @SerializedName("class")
    public HabitRpgClass _class;

    @Column
    public Double gp, exp, mp, hp;



    public BasicStats getTraining() {
        return training;
    }

    public void setTraining(BasicStats training) {
        this.training = training;
    }

    public Buffs getBuffs() {
        return buffs;
    }

    public void setBuffs(Buffs buffs) {
        this.buffs = buffs;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLvl() {
        return lvl;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }

    public HabitRpgClass get_class() {
        return _class;
    }

    public void set_class(HabitRpgClass _class) {
        this._class = _class;
    }

    public Double getGp() {
        return gp;
    }

    public void setGp(Double gp) {
        this.gp = gp;
    }

    public Double getExp() {
        return exp;
    }

    public void setExp(Double exp) {
        this.exp = exp;
    }

    public Double getMp() {
        return mp;
    }

    public void setMp(Double mp) {
        this.mp = mp;
    }

    public Double getHp() {
        return hp;
    }

    public void setHp(Double hp) {
        this.hp = hp;
    }

}

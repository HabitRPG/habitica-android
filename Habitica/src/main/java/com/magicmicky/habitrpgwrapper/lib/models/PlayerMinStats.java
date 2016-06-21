package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;

/**
 * Created by MagicMicky on 12/06/2014.
 */

public abstract class PlayerMinStats extends BasicStats {

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "trainingstats_id",
            columnType = String.class,
            foreignColumnName = "id")})
    public BasicStats training;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "buffs_id",
            columnType = String.class,
            foreignColumnName = "id")})
    public Buffs buffs;

    @Column
    public Integer points, lvl;

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

    public Integer getPoints() {
        return points != null ? points : Integer.valueOf(0);
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getLvl() {
        return lvl != null ? lvl : Integer.valueOf(0);
    }

    public void setLvl(Integer lvl) {
        this.lvl = lvl;
    }

    public HabitRpgClass get_class() {
        return _class;
    }

    public String getCleanedClassName() {
        if (_class.toString().equals("wizard")) {
            return "mage";
        }
        return _class.toString();
    }

    public void set_class(HabitRpgClass _class) {
        this._class = _class;
    }

    public Double getGp() {
        return gp != null ? gp : Double.valueOf(0);
    }

    public void setGp(Double gp) {
        this.gp = gp;
    }

    public Double getExp() {
        return exp != null ? exp : Double.valueOf(0);
    }

    public void setExp(Double exp) {
        this.exp = exp;
    }

    public Double getMp() {
        return mp != null ? mp : Double.valueOf(0);
    }

    public void setMp(Double mp) {
        this.mp = mp;
    }

    public Double getHp() {
        return hp != null ? hp : Double.valueOf(0);
    }

    public void setHp(Double hp) {
        this.hp = hp;
    }

    @Override
    public void save() {
        training.id = id+"_training";
        buffs.id = id;

        super.save();
    }

    public void merge(PlayerMinStats stats) {
        if (stats == null) {
            return;
        }
        super.merge(stats);
        this.training.merge(stats.getTraining());
        this.buffs.merge(stats.buffs);
        this.points = stats.points != null ? stats.points : this.points;
        this.lvl = stats.lvl != null ? stats.lvl : this.lvl;
        this._class = stats._class != null ? stats._class : this._class;
        this.gp = stats.gp != null ? stats.gp : this.gp;
        this.exp = stats.exp != null ? stats.exp : this.exp;
        this.hp = stats.hp != null ? stats.hp : this.hp;
        this.mp = stats.mp != null ? stats.mp : this.mp;
    }
}

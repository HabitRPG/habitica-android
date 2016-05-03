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

    @Override
    public void save() {
        training.id = id+"_training";
        buffs.id = id;

        super.save();
    }
}

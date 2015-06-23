package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MagicMicky on 12/06/2014.
 */
public class PlayerMinStats extends BasicStats {
    private BasicStats training;//stats.training
    private Buffs buffs;//stats.buffs
    private int points, lvl;
    @SerializedName("class")
    private HabitRpgClass _class;
    private Double gp, exp, mp, hp;



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



    protected class Buffs extends BasicStats {
        private boolean snowball;
        private boolean streaks;
        private Buffs() {
            this(false,false);
        }
        private Buffs(boolean snowball, boolean streaks) {
            this.snowball = snowball;
            this.streaks = streaks;
        }

        public boolean getSnowball() {
            return snowball;
        }

        public void setSnowball(boolean snowball) {
            this.snowball = snowball;
        }

        public boolean getStreaks() {
            return streaks;
        }

        public void setStreaks(boolean streaks) {
            this.streaks = streaks;
        }
    }

}

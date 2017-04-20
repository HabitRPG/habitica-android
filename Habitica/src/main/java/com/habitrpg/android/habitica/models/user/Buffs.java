package com.habitrpg.android.habitica.models.user;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.HabitDatabase;

import io.realm.RealmObject;

public class Buffs extends RealmObject {

    Stats stats;
    public Float con, str, per;
    @SerializedName("int")
    public Float _int;    private Boolean snowball;
    private Boolean streaks;
    private Boolean seafoam;
    private Boolean spookySparkles;
    private Boolean shinySeed;

    public Buffs() {
        this(false, false);
    }

    public Buffs(Boolean snowball, Boolean streaks) {
        this.snowball = snowball;
        this.streaks = streaks;
    }

    public Boolean getSnowball() {
        return snowball != null ? snowball : Boolean.FALSE;
    }

    public void setSnowball(Boolean snowball) {
        this.snowball = snowball;
    }

    public Boolean getSeafoam() {
        return seafoam != null ? seafoam : Boolean.FALSE;
    }

    public void setSeafoam(Boolean seafoam) {
        this.seafoam = seafoam;
    }

    public Boolean getSpookySparkles() {
        return spookySparkles != null ? spookySparkles : Boolean.FALSE;
    }

    public void setSpookySparkles(Boolean spookySparkles) {
        this.spookySparkles = spookySparkles;
    }

    public Boolean getShinySeed() {
        return shinySeed != null ? shinySeed : Boolean.FALSE;
    }

    public void setShinySeed(Boolean shinySeed) {
        this.shinySeed = shinySeed;
    }

    public Boolean getStreaks() {
        return streaks != null ? streaks : Boolean.FALSE;
    }

    public void setStreaks(Boolean streaks) {
        this.streaks = streaks;
    }

    public void merge(Buffs stats) {
        if (stats == null) {
            return;
        }
        this.con = stats.con != null ? stats.con : this.con;
        this.str = stats.str != null ? stats.str : this.str;
        this.per = stats.per != null ? stats.per : this.per;
        this._int = stats._int != null ? stats._int : this._int;
        this.snowball = stats.snowball != null ? stats.snowball : this.snowball;
        this.streaks = stats.streaks != null ? stats.streaks : this.streaks;
        this.seafoam = stats.seafoam != null ? stats.seafoam : this.seafoam;
        this.shinySeed = stats.shinySeed != null ? stats.shinySeed : this.shinySeed;
        this.spookySparkles = stats.spookySparkles != null ? stats.spookySparkles : this.spookySparkles;
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
}

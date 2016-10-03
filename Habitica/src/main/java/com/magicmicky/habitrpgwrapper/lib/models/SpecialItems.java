package com.magicmicky.habitrpgwrapper.lib.models;


import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class SpecialItems extends BaseModel {
    @Column
    @PrimaryKey
    @NotNull
    String user_id;

    @Column
    @NotNull
    int seafoam, shinySeed, snowball, spookySparkles;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getSeafoam() {
        return seafoam;
    }

    public void setSeafoam(int seafoam) {
        this.seafoam = seafoam;
    }

    public int getShinySeed() {
        return shinySeed;
    }

    public void setShinySeed(int shinySeed) {
        this.shinySeed = shinySeed;
    }

    public int getSnowball() {
        return snowball;
    }

    public void setSnowball(int snowball) {
        this.snowball = snowball;
    }

    public int getSpookySparkles() {
        return spookySparkles;
    }

    public void setSpookySparkles(int spookySparkles) {
        this.spookySparkles = spookySparkles;
    }
}

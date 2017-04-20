package com.habitrpg.android.habitica.models.user;


import io.realm.RealmObject;

public class SpecialItems extends RealmObject {

    Items items;
    int seafoam, shinySeed, snowball, spookySparkles;

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

    public Boolean hasSpecialItems() {
        return seafoam > 0 || shinySeed > 0 || snowball > 0 || spookySparkles > 0;
    }
}

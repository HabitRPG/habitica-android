package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmList;
import io.realm.RealmObject;

public class QuestProgress extends RealmObject {

    public String key;
    public double hp, rage;
    public RealmList<QuestProgressCollect> collect;
    public float down;
    public float up;

    public float getDown() {
        return down;
    }

    public void setDown(float down) {
        this.down = down;
    }

    public float getUp() {
        return up;
    }

    public void setUp(float up) {
        this.up = up;
    }
}

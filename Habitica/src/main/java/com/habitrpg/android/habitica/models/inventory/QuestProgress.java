package com.habitrpg.android.habitica.models.inventory;

import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class QuestProgress extends RealmObject {

    Quest quest;
    public double hp, rage;
    @Ignore
    public HashMap<String, Integer> collect;
    private float down, up;

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

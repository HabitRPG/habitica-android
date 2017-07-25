package com.habitrpg.android.habitica.models.inventory;


import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class QuestBoss extends RealmObject {

    @PrimaryKey
    private String key;
    public String name;
    public int hp;
    public float str;

    public QuestBossRage rage;

    public boolean hasRage() {
        return rage != null && rage.value != 0;
    }

    public void setKey(String key) {
        this.key = key;
        if (rage != null) {
            rage.key = key;
        }
    }
}

package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestBossRage extends RealmObject {

    @PrimaryKey
    public String key;

    public String title;

    public String description;

    public double value;

    public String tavern;

    public String stables;

    public String market;

    public QuestBossRage() {
        super();
    }
}

package com.habitrpg.android.habitica.models.inventory;


import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class QuestBoss extends RealmObject {

    @PrimaryKey
    public String key;
    public String name;
    public double hp;
    public double str;
    public double def;

    /* Boss Columns */

    public String rage_title;
    public String rage_description;
    public double rage_value;
    public String rage_tavern;
    public String rage_stables;
    public String rage_market;
    @Ignore
    public QuestBossRage rage;

    public class QuestBossRage {
        public String title;

        public String description;

        public double value;

        public String tavern;

        public String stables;

        public String market;
    }
}

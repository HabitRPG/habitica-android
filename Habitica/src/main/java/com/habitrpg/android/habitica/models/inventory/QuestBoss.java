package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class QuestBoss extends BaseModel {

    @PrimaryKey
    @Column
    public String key;

    @Column
    public String name;

    @Column
    public double hp;

    @Column
    public double str;

    @Column
    public double def;

    /* Boss Columns */

    @Column
    public String rage_title;

    @Column
    public String rage_description;

    @Column
    public double rage_value;

    @Column
    public String rage_tavern;

    @Column
    public String rage_stables;

    @Column
    public String rage_market;
    public QuestBossRage rage;

    @Override
    public void save() {
        // Just to save the Json-Object as DB-Columns

        if (rage != null && rage.title != null && !rage.title.isEmpty()) {
            rage_title = rage.title;
            rage_description = rage.description;
            rage_value = rage.value;
            rage_tavern = rage.tavern;
            rage_stables = rage.stables;
            rage_market = rage.market;
        }

        super.save();
    }

    public class QuestBossRage {
        public String title;

        public String description;

        public double value;

        public String tavern;

        public String stables;

        public String market;
    }
}

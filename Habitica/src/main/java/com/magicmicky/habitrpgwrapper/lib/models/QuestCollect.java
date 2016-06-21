package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class QuestCollect extends BaseModel {
    @Column
    public String key;

    @Column
    public String quest_key;
    @Column
    public String text;
    @Column
    public int count;
    @Column
    @PrimaryKey(autoincrement = true)
    long id;
}
